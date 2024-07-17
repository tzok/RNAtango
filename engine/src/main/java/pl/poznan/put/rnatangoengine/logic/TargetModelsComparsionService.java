package pl.poznan.put.rnatangoengine.logic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.geometry.CalcPoint;
import org.biojava.nbio.structure.geometry.SuperPositions;
import org.biojava.nbio.structure.io.CifFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.comparison.ImmutableMCQ;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.comparison.mapping.AngleDeltaMapper;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.ImmutablePdbAtomLine;
import pl.poznan.put.pdb.PdbAtomLine;
import pl.poznan.put.pdb.analysis.CifModel;
import pl.poznan.put.pdb.analysis.ImmutableDefaultCifModel;
import pl.poznan.put.pdb.analysis.ImmutablePdbCompactFragment;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.rna.NucleotideTorsionAngle;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.database.definitions.LCSEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ResidueTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.ResidueTorsionAngleRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.Angle;
import pl.poznan.put.rnatangoengine.dto.FileFormat;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelectionChain;
import pl.poznan.put.rnatangoengine.dto.LCSResult;
import pl.poznan.put.rnatangoengine.dto.NucleotideRange;
import pl.poznan.put.rnatangoengine.logic.oneManyProcessing.LcsTaProcessing;
import pl.poznan.put.structure.formats.DotBracket;
import pl.poznan.put.svg.SecondaryStructureVisualizer;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

@Service
public class TargetModelsComparsionService {
  @Autowired OneManyRepository oneManyRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired SelectionChainRepository selectionChainRepository;
  @Autowired ResidueTorsionAngleRepository residueTorsionAngleRepository;
  @Autowired LcsTaProcessing lcsProcessing;

  private StructureSelection parseStructureSelection(StructureModelEntity model)
      throws IOException {
    return StructureSelection.divideIntoCompactFragments(
        model.getFilename(),
        structureProcessingService
            .parseStructureFile(
                new String(model.getContent(), StandardCharsets.UTF_8), FileFormat.CIF)
            .getCifModels()
            .get(0)
            .residues());
  }

  private List<StructureSelection> prepareModels(List<StructureModelEntity> models)
      throws IOException {
    List<StructureSelection> structureSelections = new ArrayList<>();
    for (int i = 0; i < models.size(); i++) {
      structureSelections.add(parseStructureSelection(models.get(i)));
    }
    return structureSelections;
  }

  private List<MasterTorsionAngleType> parseAngles(List<Angle> angles) {
    return angles.stream()
        .map(
            (angle) ->
                NucleotideTorsionAngle.valueOf(
                    ExportAngleNameToAngle.parse(angle).toUpperCase().replaceAll("-", "_")))
        .collect(Collectors.toList());
  }

  private org.biojava.nbio.structure.Structure getBioJavaStructure(byte[] content)
      throws IOException {
    CifFileReader parser = new CifFileReader();
    Structure structure =
        structureProcessingService.parseStructureFile(
            new String(content, StandardCharsets.UTF_8), FileFormat.CIF);
    return parser.getStructure(
        new ByteArrayInputStream(
            structure
                .filterAuthParseCif(ImmutableSelection.builder().modelName("1").build())
                .getBytes()));
  }

  private org.biojava.nbio.structure.Structure getBioJavaStructure(
      byte[] content, NucleotideRange selection) throws IOException {
    CifFileReader parser = new CifFileReader();
    Structure structure =
        structureProcessingService.parseStructureFile(
            new String(content, StandardCharsets.UTF_8), FileFormat.CIF);
    return parser.getStructure(
        new ByteArrayInputStream(
            structure
                .filterAuthParseCif(
                    ImmutableSelection.builder()
                        .modelName("1")
                        .addChains(
                            ImmutableSelectionChain.builder()
                                .name(structure.getFirstChainName())
                                .nucleotideRange(
                                    ImmutableNucleotideRange.builder().from(selection).build())
                                .build())
                        .build())
                .getBytes()));
  }

  public LCSEntity lcs(StructureModelEntity target, StructureModelEntity model, Double threshold)
      throws IOException {

    LCSEntity lcsEntity =
        lcsProcessing.calculate(
            parseStructureSelection(target), parseStructureSelection(model), threshold);

    LCSResult lcsResult = lcsEntity.getConvertedToLCSImmutable();
    try {
      final List<PdbAtomLine> modifiedAtoms = new ArrayList<>();
      org.biojava.nbio.structure.Structure modelStructure =
          getBioJavaStructure(model.getContent(), lcsResult.modelNucleotideRange());
      org.biojava.nbio.structure.Structure targetStructure =
          getBioJavaStructure(target.getContent(), lcsResult.targetNucleotideRange());

      List<Point3d> targetPoints =
          targetStructure.getChains().get(0).getAtomGroups(GroupType.NUCLEOTIDE).stream()
              .flatMap(
                  (group) ->
                      group.getAtoms().stream()
                          .filter((atom) -> atom.getName().equals("P"))
                          .map((atom) -> atom.getCoordsAsPoint3d()))
              .collect(Collectors.toList());
      List<Point3d> modelPoints =
          modelStructure.getChains().get(0).getAtomGroups(GroupType.NUCLEOTIDE).stream()
              .flatMap(
                  (group) ->
                      group.getAtoms().stream()
                          .filter((atom) -> atom.getName().equals("P"))
                          .map((atom) -> atom.getCoordsAsPoint3d()))
              .collect(Collectors.toList());
      final Point3d[] pointsTarget = new Point3d[targetPoints.size()];
      final Point3d[] pointsModel = new Point3d[modelPoints.size()];
      for (int i = 0, size = targetPoints.size(); i < size; i++) {
        pointsTarget[i] = new Point3d(targetPoints.get(i));
      }
      for (int i = 0, size = targetPoints.size(); i < size; i++) {
        pointsModel[i] = new Point3d(modelPoints.get(i));
      }
      Matrix4d modelMatrix = SuperPositions.superposeAndTransform(pointsTarget, pointsModel);
      modelStructure = getBioJavaStructure(model.getContent());
      CifModel transformedStructureModel =
          structureProcessingService
              .parseStructureFile(
                  new String(model.getContent(), StandardCharsets.UTF_8), FileFormat.CIF)
              .getCifModels()
              .get(0);
      for (final PdbResidue residue : transformedStructureModel.residues()) {

        final List<PdbAtomLine> atoms = residue.atoms();
        final Point3d[] points = new Point3d[atoms.size()];

        for (int i = 0, size = atoms.size(); i < size; i++) {
          final PdbAtomLine atom = atoms.get(i);
          points[i] = new Point3d(atom.x(), atom.y(), atom.z());
        }
        CalcPoint.transform(modelMatrix, points);

        for (int i = 0, size = atoms.size(); i < size; i++) {
          final PdbAtomLine atom = atoms.get(i);
          modifiedAtoms.add(
              ImmutablePdbAtomLine.copyOf(atom)
                  .withX(points[i].x)
                  .withY(points[i].y)
                  .withZ(points[i].z));
        }
      }

      model.setContent(
          ImmutableDefaultCifModel.of(
                  transformedStructureModel.header(),
                  transformedStructureModel.experimentalData(),
                  transformedStructureModel.resolution(),
                  transformedStructureModel.modelNumber(),
                  modifiedAtoms,
                  transformedStructureModel.modifiedResidues(),
                  transformedStructureModel.missingResidues(),
                  transformedStructureModel.title(),
                  transformedStructureModel.chainTerminatedAfter(),
                  transformedStructureModel.basePairs())
              .toCif()
              .getBytes());
      structureModelRepository.saveAndFlush(model);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return lcsEntity;
  }

  public ResidueTorsionAngleEntity compareResidues(
      ResidueComparison residueComparison,
      List<MasterTorsionAngleType> angleTypes,
      String dotBracketSymbol) {
    ResidueTorsionAngleEntity residueTorsionAngleEntity =
        new ResidueTorsionAngleEntity(
            String.valueOf(residueComparison.model().oneLetterName()),
            residueComparison.model().residueNumber(),
            residueComparison.model().insertionCode().orElse(""));
    residueTorsionAngleEntity.setDotBracketSymbol(dotBracketSymbol);

    for (MasterTorsionAngleType angle : angleTypes) {
      TorsionAngleDelta torsionAngleDelta = residueComparison.angleDelta(angle);
      if (torsionAngleDelta.delta().isValid()) {
        residueTorsionAngleEntity.setAngle(
            ExportAngleNameToAngle.parse(torsionAngleDelta.angleType().exportName().toLowerCase()),
            Math.abs(torsionAngleDelta.delta().degrees()));
      }
    }
    Double mcq =
        residueComparison.filteredByAngleTypes(angleTypes).angleDeltas().stream()
            .filter((x) -> x.delta().isValid())
            .map((angle) -> Math.abs(angle.delta().degrees()))
            .mapToDouble(a -> a)
            .average()
            .orElse(0);
    residueTorsionAngleEntity.setMcqValue(mcq);
    return residueTorsionAngleRepository.saveAndFlush(residueTorsionAngleEntity);
  }

  public void compareModel(
      FragmentMatch fragmentMatch,
      StructureModelEntity structureModelEntity,
      List<MasterTorsionAngleType> angleTypes) {

    DotBracket dotBracket = fragmentMatch.getTargetDotBracket();
    List<ResidueTorsionAngleEntity> residueTorsionAngleEntities = new ArrayList<>();
    for (int k = 0; k < fragmentMatch.getFragmentComparison().getResidueComparisons().size(); k++) {

      residueTorsionAngleEntities.add(
          compareResidues(
              fragmentMatch.getFragmentComparison().getResidueComparisons().get(k),
              angleTypes,
              String.valueOf(dotBracket.structure().charAt(k))));
    }

    try {
      structureModelEntity.setSecondaryStructureVisualizationSVG(
          SVGHelper.export(
              SecondaryStructureVisualizer.visualize(fragmentMatch, AngleDeltaMapper.getInstance()),
              Format.SVG));
    } catch (Exception el) {
      el.printStackTrace();
    }
    residueTorsionAngleRepository.saveAllAndFlush(residueTorsionAngleEntities);

    structureModelEntity.addResidueEntities(residueTorsionAngleEntities);
    structureModelEntity.setMcqValue(
        fragmentMatch.getFragmentComparison().getResidueComparisons().stream()
            .map(
                (residue) ->
                    residue.validDeltas().stream()
                        .filter((angle) -> angle.isValid())
                        .map((angle) -> Math.abs(angle.degrees()))
                        .collect(Collectors.toList()))
            .flatMap(List::stream)
            .mapToDouble(a -> a)
            .average()
            .orElse(0));
    structureModelRepository.saveAndFlush(structureModelEntity);
  }

  public ModelsComparisonResult compare(
      StructureModelEntity targetModelEntity,
      List<StructureModelEntity> modelsEntities,
      List<Angle> angles)
      throws IOException {

    StructureSelection target = parseStructureSelection(targetModelEntity);
    List<StructureSelection> models = prepareModels(modelsEntities);

    List<MasterTorsionAngleType> angleTypes = parseAngles(angles);

    ModelsComparisonResult comparisonResult = compareFragment(target, models, angleTypes, 0);

    for (int j = 0; j < comparisonResult.fragmentMatches().size(); j++) {
      compareModel(comparisonResult.fragmentMatches().get(j), modelsEntities.get(j), angleTypes);
    }

    return comparisonResult;
  }

  private static ImmutablePdbCompactFragment renamedInstance(
      final StructureSelection selection, boolean isModel, final int i) {
    final List<PdbCompactFragment> compactFragments = selection.getCompactFragments();
    final PdbCompactFragment compactFragment = compactFragments.get(i);
    final String name =
        isModel
            ? (compactFragments.size() == 1
                ? selection.getName() + "_model"
                : String.format("%s %s", selection.getName() + "_model", compactFragment.name()))
            : (compactFragments.size() == 1
                ? selection.getName()
                : String.format("%s %s", selection.getName(), compactFragment.name()));
    return ImmutablePdbCompactFragment.copyOf(compactFragment).withName(name);
  }

  private static ModelsComparisonResult compareFragment(
      StructureSelection target,
      List<StructureSelection> models,
      List<MasterTorsionAngleType> angleTypes,
      final int i) {
    final PdbCompactFragment targetFragment = renamedInstance(target, false, i);
    final List<PdbCompactFragment> modelFragments =
        models.stream().map(model -> renamedInstance(model, true, i)).collect(Collectors.toList());
    return ImmutableMCQ.of(MoleculeType.RNA)
        .withAngleTypes(angleTypes)
        .compareModels(targetFragment, modelFragments);
  }
}
