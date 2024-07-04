package pl.poznan.put.rnatangoengine.logic.oneManyProcessing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.circular.samples.ImmutableAngleSample;
import pl.poznan.put.comparison.ImmutableMCQ;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.ImmutablePdbCompactFragment;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.rna.NucleotideTorsionAngle;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.FileFormat;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.torsion.MasterTorsionAngleType;

@Service
public class OneManyProcessing {

  @Autowired OneManyRepository oneManyRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired SelectionChainRepository selectionChainRepository;

  public void startTask(UUID taskHashId) throws Exception {

    Structure targetStructure;
    Structure modelStructure;
    OneManyResultEntity oneManyResultEntity = oneManyRepository.getByHashId(taskHashId);
    // if (!oneManyResultEntity.getStatus().equals(Status.WAITING)) {
    //   return;
    // }
    // oneManyResultEntity.setStatus(Status.PROCESSING);
    oneManyRepository.save(oneManyResultEntity);
    StructureModelEntity target;
    List<StructureSelection> modelsSelection = new ArrayList<>();
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
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Target processing error");
    }
    for (StructureModelEntity model : oneManyResultEntity.getModels()) {
      int startIndex = model.getFilteredSequence().indexOf(target.getFilteredSequence());
      SelectionChainEntity selectionChain = model.getSelection().getSelectionChains().get(0);
      selectionChain.setFromInclusive(selectionChain.getFromInclusive() + startIndex);
      selectionChain.setToInclusive(
          selectionChain.getFromInclusive()
              + startIndex
              + target.getFilteredSequence().length()
              - 1);
      model.setFilteredSequence(target.getFilteredSequence());
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
    StructureSelection targetStructureSelection =
        StructureSelection.divideIntoCompactFragments(
            target.getFilename(), targetStructure.getCifModels().get(0).residues());
    List<MasterTorsionAngleType> masterTorsionAngleTypes =
        oneManyResultEntity.getAnglesToAnalyze().stream()
            .map(
                (angle) ->
                    NucleotideTorsionAngle.valueOf(
                        ExportAngleNameToAngle.parse(angle).toUpperCase()))
            .collect(Collectors.toList());
    // List<MasterTorsionAngleType> masterTorsionAngleTypes=new Ma
    compare(targetStructureSelection, modelsSelection, masterTorsionAngleTypes);
    // try {
    //

    //
    // structure =
    // structureProcessingService.process(singleResultEntity.getFileId());
    // singleResultEntity.setStructureName(structure.getStructureName());
    // singleResultEntity.setStructureMolecule(structure.getStructureMoleculeName());
    // singleResultEntity.setStructureFileContent(
    // structure
    // .filterParseCif(
    // singleResultEntity.getSelections().stream()
    // .map((s) -> s.getConvertedToSelectionImmutable())
    // .collect(Collectors.toList()))
    // .getBytes());
    //
    // singleRepository.save(singleResultEntity);
    //
    // List<ChainTorsionAngleEntity> structureSingleProcessingTorsionAngles =
    // new ArrayList<ChainTorsionAngleEntity>();
    // ExportAngleNameToAngle exportAngleNameToAngle = new ExportAngleNameToAngle();
    //
    // File tempFile = File.createTempFile("structure", ".cif");
    //
    // BufferedWriter writer = new BufferedWriter(new
    // FileWriter(tempFile.getAbsolutePath()));
    // writer.write(
    // new String(singleResultEntity.getStructureFileContent(),
    // StandardCharsets.UTF_8));
    // writer.close();
    //
    // for (SelectionEntity selection : singleResultEntity.getSelections()) {
    // PdbModel model =
    // StructureManager.loadStructure(tempFile)
    // .get(Integer.valueOf(selection.getModelName()) - 1);
    // for (final PdbChain chain : model.chains()) {
    // ChainTorsionAngleEntity chainTorsionAngleEntity =
    // new ChainTorsionAngleEntity(chain.identifier(), chain.sequence());
    // chainTorsionAngleRepository.save(chainTorsionAngleEntity);
    // ImmutablePdbCompactFragment fragment =
    // ImmutablePdbCompactFragment.of(chain.residues());
    // List<ResidueTorsionAngleEntity> residueAngles =
    // new ArrayList<ResidueTorsionAngleEntity>();
    //
    // for (final PdbResidue residue : fragment.residues()) {
    // final ResidueTorsionAngles residueTorsionAngles =
    // fragment.torsionAngles(residue.identifier());
    //
    // ResidueTorsionAngleEntity _residueTorsionAngleEntity =
    // residueTorsionAngleRepository.save(
    // new ResidueTorsionAngleEntity(
    // residue.modifiedResidueName(),
    // residue.residueNumber(),
    // residue.insertionCode().orElse("")));
    //
    // MoleculeType.RNA.allAngleTypes().stream()
    // .forEach(
    // (residueAngle) ->
    // _residueTorsionAngleEntity.setAngle(
    // exportAngleNameToAngle.parse(residueAngle.exportName()),
    // (residueTorsionAngles.value(residueAngle).isValid()
    // ? residueTorsionAngles.value(residueAngle).degrees()
    // : null)));
    // residueAngles.add(_residueTorsionAngleEntity);
    // }
    //
    // chainTorsionAngleEntity.getResiduesTorsionAngles().addAll(residueAngles);
    //
    // structureSingleProcessingTorsionAngles.add(chainTorsionAngleEntity);
    // }
    // }
    //
    // singleResultEntity.getChainTorsionAngles().addAll(structureSingleProcessingTorsionAngles);
    //
    // singleResultEntity.setStatus(Status.SUCCESS);
    // singleResultEntity.setIsDiscontinuousResiduesSequence(
    // structure.getContainDiscontinuousScopes());
    // singleRepository.save(singleResultEntity);
    // tempFile.deleteOnExit();
    // try {
    // fileRepository.deleteByHashId(UUID.fromString(singleResultEntity.getFileId()));
    // } catch (Exception e) {
    // }
    // } catch (IOException e) {
    // singleResultEntity.setStatus(Status.FAILED);
    // singleResultEntity.setErrorLog(e.getStackTrace().toString());
    // singleResultEntity.setUserErrorLog("Error during structure processing");
    //
    // singleRepository.save(singleResultEntity);
    // } catch (IllegalArgumentException e) {
    // singleResultEntity.setStatus(Status.FAILED);
    // singleResultEntity.setErrorLog(e.getStackTrace().toString());
    // singleResultEntity.setUserErrorLog("Residues does not have atoms
    // coordinates");
    //
    // singleRepository.save(singleResultEntity);
    // }
  }

  private void compare(
      StructureSelection target,
      List<StructureSelection> models,
      List<MasterTorsionAngleType> angleTypes) {
    // compare each compact fragment
    List<ModelsComparisonResult> comparisonResults = new ArrayList<>();
    for (int i = 0; i < target.getCompactFragments().size(); i++) {
      comparisonResults.add(compareFragment(target, models, angleTypes, i));
    }

    // compare mcq for each model
    final List<Angle> mcqs =
        IntStream.range(0, models.size())
            .mapToObj(
                i ->
                    comparisonResults.stream()
                        .map(ModelsComparisonResult::fragmentMatches)
                        .map(fragmentMatches -> fragmentMatches.get(i))
                        .map(FragmentMatch::getResidueComparisons)
                        .flatMap(Collection::stream)
                        .map(ResidueComparison::validDeltas)
                        .flatMap(Collection::stream)
                        .filter(Angle::isValid)
                        .collect(Collectors.toList()))
            .map(ImmutableAngleSample::of)
            .map(AngleSample::meanDirection)
            .collect(Collectors.toList());

    // generate ranking
    final List<Pair<Double, StructureSelection>> ranking =
        IntStream.range(0, models.size())
            .mapToObj(i -> Pair.of(mcqs.get(i).degrees(), models.get(i)))
            .sorted(Comparator.comparingDouble(Pair::getLeft))
            .collect(Collectors.toList());

    for (final Pair<Double, StructureSelection> pair : ranking) {
      System.out.printf(Locale.US, "%s %.2f%n", pair.getValue().getName(), pair.getKey());
    }
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

  private void exportResults(final ModelsComparisonResult comparisonResult) {
    try {
      exportTable(comparisonResult);
      comparisonResult.fragmentMatches().forEach(this::exportModelResults);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Failed to export results", e);
    }
  }

  private void exportModelResults(final FragmentMatch fragmentMatch) {
    try {
      final String name = fragmentMatch.getModelFragment().name();
      // final File directory = new File(outputDirectory().toFile(), name);
      // FileUtils.forceMkdir(directory);

      // Local.exportSecondaryStructureImage(
      //     fragmentMatch, directory, "delta.svg", AngleDeltaMapper.getInstance());
      // Local.exportSecondaryStructureImage(
      //     fragmentMatch, directory, "range.svg", RangeDifferenceMapper.getInstance());

      // exportDifferences(fragmentMatch, directory);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Failed to export results", e);
    }
  }

  private void exportTable(final ModelsComparisonResult comparisonResult) throws IOException {
    // final File file = new File(outputDirectory().toFile(), "table.csv");
    // try (final OutputStream stream = Files.newOutputStream(file.toPath())) {
    //   final SelectedAngle selectedAngle = comparisonResult.selectAverageOfAngles();
    //   selectedAngle.export(stream);
    // }
  }
}
