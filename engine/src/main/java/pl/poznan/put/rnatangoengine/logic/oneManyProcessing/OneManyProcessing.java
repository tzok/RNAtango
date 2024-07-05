package pl.poznan.put.rnatangoengine.logic.oneManyProcessing;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.comparison.ImmutableMCQ;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.comparison.mapping.AngleDeltaMapper;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.ImmutablePdbCompactFragment;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.rna.NucleotideTorsionAngle;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.database.definitions.ResidueTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.ResidueTorsionAngleRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.FileFormat;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.structure.formats.DotBracket;
import pl.poznan.put.svg.SecondaryStructureVisualizer;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

@Service
public class OneManyProcessing {

  @Autowired OneManyRepository oneManyRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired SelectionChainRepository selectionChainRepository;
  @Autowired ResidueTorsionAngleRepository residueTorsionAngleRepository;
  @Autowired LcsProcessing lcsProcessing;

  public void startTask(UUID taskHashId) throws Exception {

    OneManyResultEntity oneManyResultEntity = oneManyRepository.getByHashId(taskHashId);
    if (!oneManyResultEntity.getStatus().equals(Status.WAITING)) {
      return;
    }
    oneManyResultEntity.setStatus(Status.PROCESSING);
    oneManyRepository.save(oneManyResultEntity);

    process(oneManyResultEntity);
  }

  private List<StructureSelection> prepareModels(
      List<StructureModelEntity> models, String targetFilteredSequence) throws IOException {
    Structure modelStructure;
    List<StructureSelection> modelsSelection = new ArrayList<>();
    for (StructureModelEntity model : models) {
      int startIndex = model.getFilteredSequence().indexOf(targetFilteredSequence);
      SelectionChainEntity selectionChain = model.getSelection().getSelectionChains().get(0);
      selectionChain.setFromInclusive(selectionChain.getFromInclusive() + startIndex);
      selectionChain.setToInclusive(
          selectionChain.getFromInclusive() + startIndex + targetFilteredSequence.length() - 1);
      model.setFilteredSequence(targetFilteredSequence);
      selectionChainRepository.saveAndFlush(selectionChain);
      modelStructure =
          structureProcessingService.parseStructureFile(
              new String(model.getContent(), StandardCharsets.UTF_8), FileFormat.CIF);
      model.setContent(
          modelStructure
              .filterAuthParseCif(model.getSelection().getConvertedToSelectionImmutable())
              .getBytes());
      model = structureModelRepository.saveAndFlush(model);
      modelsSelection.add(
          StructureSelection.divideIntoCompactFragments(
              model.getFilename(), modelStructure.getCifModels().get(0).residues()));
    }
    return modelsSelection;
  }

  private void process(OneManyResultEntity oneManyResultEntity) throws Exception {
    Structure targetStructure;

    StructureModelEntity target;
    try {
      target = oneManyResultEntity.getTargetEntity();
      targetStructure =
          structureProcessingService.parseStructureFile(
              new String(target.getContent(), StandardCharsets.UTF_8), FileFormat.CIF);
      target.setContent(
          targetStructure
              .filterAuthParseCif(target.getSelection().getConvertedToSelectionImmutable())
              .getBytes());
      target.setFilteredSequence(targetStructure.getModelSequence());
      target = structureModelRepository.saveAndFlush(target);

      StructureSelection targetStructureSelection =
          StructureSelection.divideIntoCompactFragments(
              target.getFilename(), targetStructure.getCifModels().get(0).residues());

      List<MasterTorsionAngleType> masterTorsionAngleTypes =
          oneManyResultEntity.getAnglesToAnalyze().stream()
              .map(
                  (angle) ->
                      NucleotideTorsionAngle.valueOf(
                          ExportAngleNameToAngle.parse(angle).toUpperCase().replaceAll("-", "_")))
              .collect(Collectors.toList());

      List<StructureSelection> modelsSelection =
          prepareModels(oneManyResultEntity.getModels(), target.getFilteredSequence());

      List<ModelsComparisonResult> comparisonResults =
          compare(
              targetStructureSelection,
              modelsSelection,
              masterTorsionAngleTypes,
              oneManyResultEntity);

      oneManyResultEntity.setFinalSequence(target.getFilteredSequence());
      oneManyResultEntity.setFinalStructure(
          comparisonResults.get(0).fragmentMatches().get(0).getTargetDotBracket().structure());
      oneManyResultEntity.setStatus(Status.SUCCESS);
      oneManyRepository.saveAndFlush(oneManyResultEntity);
    } catch (Exception e) {
      oneManyResultEntity.setStatus(Status.FAILED);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);

      oneManyResultEntity.setErrorLog(sw.toString().substring(0, 4999));
      oneManyResultEntity.setUserErrorLog("Error during proecessing request");
      oneManyRepository.saveAndFlush(oneManyResultEntity);
      e.printStackTrace();
    }
  }

  private List<ModelsComparisonResult> compare(
      StructureSelection target,
      List<StructureSelection> models,
      List<MasterTorsionAngleType> angleTypes,
      OneManyResultEntity oneManyResultEntity) {
    // compare each compact fragment
    List<ModelsComparisonResult> comparisonResults = new ArrayList<>();
    List<StructureModelEntity> structureModels = oneManyResultEntity.getModels();
    for (int i = 0; i < target.getCompactFragments().size(); i++) {
      ModelsComparisonResult compareResult = compareFragment(target, models, angleTypes, i);

      for (int j = 0; j < compareResult.fragmentMatches().size(); j++) {
        FragmentMatch fragmentMatch = compareResult.fragmentMatches().get(j);
        StructureModelEntity structureModelEntity = structureModels.get(j);
        DotBracket dotBracket = fragmentMatch.getTargetDotBracket();
        structureModelEntity.setLcsResult(
            lcsProcessing.calculate(target, models.get(j), oneManyResultEntity.getThreshold()));
        List<ResidueTorsionAngleEntity> residueTorsionAngleEntities = new ArrayList<>();
        for (int k = 0;
            k < fragmentMatch.getFragmentComparison().getResidueComparisons().size();
            k++) {
          ResidueComparison residueComparison =
              fragmentMatch.getFragmentComparison().getResidueComparisons().get(k);
          ResidueTorsionAngleEntity residueTorsionAngleEntity =
              new ResidueTorsionAngleEntity(
                  String.valueOf(residueComparison.model().oneLetterName()),
                  residueComparison.model().residueNumber(),
                  residueComparison.model().insertionCode().orElse(""));
          residueTorsionAngleEntity.setDotBracketSymbol(
              String.valueOf(dotBracket.structure().charAt(k)));

          for (MasterTorsionAngleType angle : angleTypes) {
            TorsionAngleDelta torsionAngleDelta = residueComparison.angleDelta(angle);
            if (torsionAngleDelta.delta().isValid()) {
              residueTorsionAngleEntity.setAngle(
                  ExportAngleNameToAngle.parse(
                      torsionAngleDelta.angleType().exportName().toLowerCase()),
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
          residueTorsionAngleEntities.add(residueTorsionAngleEntity);
        }

        try {
          structureModelEntity.setSecondaryStructureVisualizationSVG(
              SVGHelper.export(
                  SecondaryStructureVisualizer.visualize(
                      fragmentMatch, AngleDeltaMapper.getInstance()),
                  Format.SVG));
        } catch (Exception el) {
          el.printStackTrace();
        }
        residueTorsionAngleRepository.saveAllAndFlush(residueTorsionAngleEntities);

        structureModelEntity.addResidueEntities(residueTorsionAngleEntities);
        structureModelRepository.saveAndFlush(structureModelEntity);
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
      }
      comparisonResults.add(compareResult);
    }

    return comparisonResults;
  }

  private static ImmutablePdbCompactFragment renamedInstance(
      final StructureSelection selection, final int i) {
    final List<PdbCompactFragment> compactFragments = selection.getCompactFragments();
    final PdbCompactFragment compactFragment = compactFragments.get(i);
    final String name =
        compactFragments.size() == 1
            ? selection.getName()
            : String.format("%s %s", selection.getName(), compactFragment.name());
    return ImmutablePdbCompactFragment.copyOf(compactFragment).withName(name);
  }

  private ModelsComparisonResult compareFragment(
      StructureSelection target,
      List<StructureSelection> models,
      List<MasterTorsionAngleType> angleTypes,
      final int i) {
    final PdbCompactFragment targetFragment = renamedInstance(target, i);
    final List<PdbCompactFragment> modelFragments =
        models.stream().map(model -> renamedInstance(model, i)).collect(Collectors.toList());
    return ImmutableMCQ.of(MoleculeType.RNA)
        .withAngleTypes(angleTypes)
        .compareModels(targetFragment, modelFragments);
  }
}
