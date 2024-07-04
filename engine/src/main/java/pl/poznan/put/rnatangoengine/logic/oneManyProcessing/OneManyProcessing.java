package pl.poznan.put.rnatangoengine.logic.oneManyProcessing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

@Service
public class OneManyProcessing {

  @Autowired OneManyRepository oneManyRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired SelectionChainRepository selectionChainRepository;
  @Autowired ResidueTorsionAngleRepository residueTorsionAngleRepository;

  public void startTask(UUID taskHashId) throws Exception {

    Structure targetStructure;
    Structure modelStructure;
    OneManyResultEntity oneManyResultEntity = oneManyRepository.getByHashId(taskHashId);
    if (!oneManyResultEntity.getStatus().equals(Status.WAITING)) {
      return;
    }
    oneManyResultEntity.setStatus(Status.PROCESSING);
    oneManyRepository.save(oneManyResultEntity);
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

      List<StructureSelection> modelsSelection = new ArrayList<>();
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
      List<ModelsComparisonResult> comparisonResults =
          compare(
              targetStructureSelection,
              modelsSelection,
              masterTorsionAngleTypes,
              oneManyResultEntity);
      comparisonResults.get(0).fragmentMatches().get(0).getTargetDotBracket();
      DotBracket dotBracket =
          comparisonResults.get(0).fragmentMatches().get(0).getTargetDotBracket();
      oneManyResultEntity.setFinalSequence(dotBracket.sequence());
      oneManyResultEntity.setFinalStructure(dotBracket.structure());
      oneManyResultEntity.setStatus(Status.SUCCESS);

      oneManyRepository.saveAndFlush(oneManyResultEntity);
    } catch (Exception e) {
      oneManyResultEntity.setStatus(Status.FAILED);
      oneManyRepository.saveAndFlush(oneManyResultEntity);
      e.printStackTrace();
      throw new Exception("Target processing error");
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
      comparisonResults.add(compareResult);
      // exportTable(compareResult);
      // SelectedAngle selectedAngle = compareResult.selectAverageOfAngles();
      // selectedAngle.export(null);

      for (int j = 0; j < compareResult.fragmentMatches().size(); j++) {
        FragmentMatch fragmentMatch = compareResult.fragmentMatches().get(j);
        StructureModelEntity structureModelEntity = structureModels.get(j);
        DotBracket dotBracket = fragmentMatch.getTargetDotBracket();
        List<ResidueTorsionAngleEntity> residueTorsionAngleEntities = new ArrayList<>();
        Double mcqForModel = 0.0;
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
                  torsionAngleDelta.delta().degrees360());
            }
          }
          Double mcq =
              residueComparison.filteredByAngleTypes(angleTypes).angleDeltas().stream()
                  .filter((x) -> x.delta().isValid())
                  .map((angle) -> angle.delta().degrees360())
                  .reduce(0.0, Double::sum);
          residueTorsionAngleEntity.setMcqValue(mcq);
          mcqForModel += mcq;
          residueTorsionAngleEntities.add(residueTorsionAngleEntity);
        }

        // try {
        //   structureModelEntity.setSecondaryStructureVisualizationSVG(
        //       SVGHelper.export(
        //           SecondaryStructureVisualizer.visualize(
        //               fragmentMatch, (ComparisonMapper) AngleDeltaMapper.getInstance()),
        //           Format.SVG));
        // } catch (Exception el) {
        //   el.printStackTrace();
        // }
        residueTorsionAngleRepository.saveAllAndFlush(residueTorsionAngleEntities);

        structureModelEntity.addResidueEntities(residueTorsionAngleEntities);
        structureModelRepository.saveAndFlush(structureModelEntity);
        structureModelEntity.setMcqValue(
            mcqForModel / fragmentMatch.getFragmentComparison().getResidueComparisons().size());
      }
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

    // // generate ranking
    // final List<Pair<Double, StructureSelection>> ranking =
    //     IntStream.range(0, models.size())
    //         .mapToObj(i -> Pair.of(mcqs.get(i).degrees(), models.get(i)))
    //         .sorted(Comparator.comparingDouble(Pair::getLeft))
    //         .collect(Collectors.toList());

    // for (final Pair<Double, StructureSelection> pair : ranking) {
    //   System.out.printf(Locale.US, "%s %.2f%n", pair.getValue().getName(), pair.getKey());
    // }
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

  // private void exportResults(final ModelsComparisonResult comparisonResult) {
  //   try {
  //     exportTable(comparisonResult);
  //     comparisonResult.fragmentMatches().forEach(this::exportModelResults);
  //   } catch (final IOException e) {
  //     throw new IllegalArgumentException("Failed to export results", e);
  //   }
  // }

  // // private void exportModelResults(final FragmentMatch fragmentMatch) {
  // //   try {
  // //     final String name = fragmentMatch.getModelFragment().name();
  // //     // final File directory = new File(outputDirectory().toFile(), name);
  // //     // FileUtils.forceMkdir(directory);

  // //     // Local.exportSecondaryStructureImage(
  // //     //     fragmentMatch, directory, "delta.svg", AngleDeltaMapper.getInstance());
  // //     // Local.exportSecondaryStructureImage(
  // //     //     fragmentMatch, directory, "range.svg", RangeDifferenceMapper.getInstance());

  // //     // exportDifferences(fragmentMatch, directory);
  // //   } catch (final Exception e) {
  // //     throw new IllegalArgumentException("Failed to export results", e);
  // //   }
  // // }

}
