package pl.poznan.put.rnatangoengine.logic.manyManyProcessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.put.clustering.partitional.KMedoids;
import pl.poznan.put.clustering.partitional.PAM;
import pl.poznan.put.clustering.partitional.PartitionalClustering;
import pl.poznan.put.clustering.partitional.PrototypeBasedClusterer;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.comparison.ImmutableMCQ;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.rna.NucleotideTorsionAngle;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.database.definitions.ClusteredModelEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ClusteringResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.ClusteredModelRepository;
import pl.poznan.put.rnatangoengine.database.repository.ClusteringResultRepository;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.Angle;
import pl.poznan.put.rnatangoengine.dto.FileFormat;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelectionChain;
import pl.poznan.put.rnatangoengine.dto.IndexPair;
import pl.poznan.put.rnatangoengine.dto.Selection;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.logic.TargetModelsComparsionService;
import pl.poznan.put.rnatangoengine.logic.oneManyProcessing.OneManyProcessing;
import pl.poznan.put.rnatangoengine.service.SelectionService;
import pl.poznan.put.rnatangoengine.service.StructureModelService;
import pl.poznan.put.svg.MDSDrawer;
import pl.poznan.put.torsion.MasterTorsionAngleType;

@Service
public class ManyManyProcessing {

  @Autowired ManyManyRepository manyManyRepository;
  @Autowired OneManyRepository oneManyRepository;
  @Autowired OneManyProcessing oneManyProcessing;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired SelectionChainRepository selectionChainRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureModelService structureModelService;
  @Autowired SelectionService selectionService;
  @Autowired TargetModelsComparsionService targetModelsComparsionService;
  @Autowired ClusteringResultRepository clusteringResultRepository;
  @Autowired ClusteredModelRepository clusteredModelRepository;

  public void startTask(UUID taskHashId) {

    ManyManyResultEntity manyManyResultEntity = manyManyRepository.getByHashId(taskHashId);
    if (!manyManyResultEntity.getStatus().equals(Status.WAITING)) {
      return;
    }
    manyManyResultEntity.setStatus(Status.PROCESSING);
    manyManyRepository.save(manyManyResultEntity);

    process(manyManyResultEntity);
  }

  private void applyCommonSequence(
      StructureModelEntity structureModelEntity, String sequence, String chain) {

    for (SelectionChainEntity selectionChainEntity :
        structureModelEntity.getSourceSelection().getSelectionChains()) {
      if (selectionChainEntity.getName().equals(chain)) {
        int index = selectionChainEntity.getSequence().indexOf(sequence);
        if (index > -1) {
          Selection selection =
              ImmutableSelection.builder()
                  .modelName("1")
                  .addChains(
                      ImmutableSelectionChain.builder()
                          .name(chain)
                          .sequence(sequence)
                          .nucleotideRange(
                              ImmutableNucleotideRange.builder()
                                  .fromInclusive(selectionChainEntity.getFromInclusive() + index)
                                  .toInclusive(
                                      selectionChainEntity.getFromInclusive()
                                          + index
                                          + sequence.length()
                                          - 1)
                                  .build())
                          .build())
                  .build();

          SelectionEntity selectionEntity =
              selectionRepository.saveAndFlush(new SelectionEntity(selection));
          structureModelEntity.setSelection(selectionEntity);
          structureModelEntity.setFilteredSequence(sequence);
          structureModelEntity.setTargetRangeRelative(new IndexPair(0, sequence.length() - 1));
          structureModelRepository.saveAndFlush(structureModelEntity);
          break;
        }
      }
    }
  }

  private List<MasterTorsionAngleType> parseAngles(List<Angle> angles) {
    return angles.stream()
        .map(
            (angle) ->
                NucleotideTorsionAngle.valueOf(
                    ExportAngleNameToAngle.parse(angle).toUpperCase().replaceAll("-", "_")))
        .collect(Collectors.toList());
  }

  private StructureSelection parseStructureSelection(StructureModelEntity model) {
    try {
      return StructureSelection.divideIntoCompactFragments(
          model.getFilename(),
          structureProcessingService
              .parseStructureFile(
                  new String(model.getContent(), StandardCharsets.UTF_8), FileFormat.CIF)
              .getCifModels()
              .get(0)
              .residues());
    } catch (IOException e) {
      return StructureSelection.divideIntoCompactFragments(model.getFilename(), new ArrayList<>());
    }
  }

  private byte[] renderDendrogram(double[][] distanceMatrix, List<String> modelNames) {
    List<Double> flattenMatrix = new ArrayList<>();
    for (int i = 0; i < distanceMatrix.length; i++) {
      for (int j = 0; j < distanceMatrix.length; j++) {
        flattenMatrix.add(distanceMatrix[i][j]);
      }
    }
    ProcessBuilder processBuilder =
        new ProcessBuilder(
            "python3",
            "/opt/rnatango/dendrogram.py",
            flattenMatrix.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(",")),
            modelNames.stream().collect(Collectors.joining(",")));

    processBuilder.redirectErrorStream(true);
    try {
      Process process = processBuilder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = "";
      String outputString = "";
      while ((line = reader.readLine()) != null) {

        outputString = outputString.concat(line);
      }
      process.waitFor();
      return outputString.getBytes();
    } catch (Exception e) {
      e.printStackTrace();
      return "".getBytes();
    }
  }

  @Transactional
  private void globalProcessing(ManyManyResultEntity manyManyResultEntity) {

    final GlobalAnalysis comparator =
        new GlobalAnalysis(
            ImmutableMCQ.of(MoleculeType.RNA)
                .withAngleTypes(parseAngles(manyManyResultEntity.getAnglesToAnalyze())),
            manyManyResultEntity.getModels().stream()
                .map(model -> parseStructureSelection(model))
                .collect(Collectors.toList()));

    try {
      comparator.run();
      comparator.join();

      final double[][] rawMatrix = comparator.getResult().getDistanceMatrix().matrix();

      if (rawMatrix.length > 2) {
        final PrototypeBasedClusterer clusterer = new KMedoids();

        for (int k = 2; k <= Math.min(12, rawMatrix.length - 1); k++) {
          ClusteringResultEntity clusteringResultEntity = new ClusteringResultEntity(k);
          final ScoredClusteringResult clustering =
              clusterer.findPrototypes(rawMatrix, PAM.getInstance(), k);
          final PartitionalClustering partitionalClustering =
              new PartitionalClustering(comparator.getResult().getDistanceMatrix(), clustering);
          List<ClusteredModelEntity> models = new ArrayList<>();
          double[][] spatialReptresentation =
              MDSDrawer.scaleTo2D(partitionalClustering.getDistanceMatrix());
          for (int i = 0; i < rawMatrix.length; i++) {
            models.add(
                new ClusteredModelEntity(
                    partitionalClustering.getAssignment().getPrototype(i),
                    manyManyResultEntity.getModels().get(i).getFilename(),
                    spatialReptresentation[i][0],
                    spatialReptresentation[i][1]));
          }
          clusteringResultEntity = clusteringResultRepository.saveAndFlush(clusteringResultEntity);
          clusteringResultEntity.setModels(clusteredModelRepository.saveAllAndFlush(models));

          manyManyResultEntity.addClustering(
              clusteringResultRepository.saveAndFlush(clusteringResultEntity));
        }
      }

      manyManyResultEntity.setDendrogram(
          renderDendrogram(
              rawMatrix,
              manyManyResultEntity.getModels().stream()
                  .map((model) -> model.getFilename())
                  .collect(Collectors.toList())));
      manyManyRepository.saveAndFlush(manyManyResultEntity);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private OneManyResultEntity prepareOneManyInstances(
      StructureModelEntity target,
      List<StructureModelEntity> models,
      ManyManyResultEntity manyManyResultEntity) {

    StructureModelEntity targetDetached =
        structureModelRepository.saveAndFlush(new StructureModelEntity(target));
    targetDetached.setSelection(
        selectionRepository.saveAndFlush(
            selectionService.createSelectionEntity(
                target.getSelection().getConvertedToSelectionImmutable())));
    targetDetached.setSourceSelection(
        selectionRepository.saveAndFlush(
            selectionService.createSelectionEntity(
                target.getSourceSelection().getConvertedToSelectionImmutable())));
    OneManyResultEntity oneManyResultEntity =
        oneManyRepository.saveAndFlush(
            new OneManyResultEntity(
                structureModelRepository.saveAndFlush(targetDetached),
                "1",
                targetDetached.getSelection().getModelName()));

    for (StructureModelEntity model : models) {
      StructureModelEntity m =
          structureModelRepository.saveAndFlush(new StructureModelEntity(model));
      m.setSelection(
          selectionService.createSelectionEntity(
              model.getSelection().getConvertedToSelectionImmutable()));
      m.setSourceSelection(
          selectionService.createSelectionEntity(
              model.getSourceSelection().getConvertedToSelectionImmutable()));
      oneManyResultEntity.addModel(structureModelRepository.saveAndFlush(m));
    }

    oneManyResultEntity.setAnglesToAnalyze(manyManyResultEntity.getAnglesToAnalyze());
    oneManyResultEntity.setThreshold(manyManyResultEntity.getThreshold());
    oneManyResultEntity.setStatus(Status.WAITING);
    return oneManyRepository.saveAndFlush(oneManyResultEntity);
  }

  private void process(ManyManyResultEntity manyManyResultEntity) {
    try {
      for (StructureModelEntity structureModelEntity : manyManyResultEntity.getModels()) {
        applyCommonSequence(
            structureModelEntity,
            manyManyResultEntity.getSequenceToAnalyze(),
            manyManyResultEntity.getChainToAnalyze());
        structureModelService.filterModelContent(
            structureModelRepository.getByHashId(structureModelEntity.getHashId()));
      }
      manyManyResultEntity = manyManyRepository.getByHashId(manyManyResultEntity.getHashId());
      List<StructureModelEntity> modelEntities = manyManyResultEntity.getModels();
      for (int i = 0; i < modelEntities.size(); i++) {
        final int e = i;
        manyManyResultEntity.addOneManyInstance(
            prepareOneManyInstances(
                modelEntities.get(i),
                IntStream.range(0, modelEntities.size())
                    .filter(j -> e != j)
                    .mapToObj(modelEntities::get)
                    .collect(Collectors.toList()),
                manyManyResultEntity));
      }
      manyManyResultEntity = manyManyRepository.saveAndFlush(manyManyResultEntity);

      for (OneManyResultEntity oneManyResultEntity :
          manyManyRepository.getByHashId(manyManyResultEntity.getHashId()).getAllComparations()) {
        oneManyProcessing.process(oneManyResultEntity);
      }
      globalProcessing(manyManyRepository.getByHashId(manyManyResultEntity.getHashId()));
      manyManyResultEntity = manyManyRepository.getByHashId(manyManyResultEntity.getHashId());
      manyManyResultEntity.setStatus(Status.SUCCESS);
      manyManyRepository.saveAndFlush(manyManyResultEntity);
    } catch (Exception e) {
      manyManyResultEntity = manyManyRepository.getByHashId(manyManyResultEntity.getHashId());
      manyManyResultEntity.setStatus(Status.FAILED);
      manyManyResultEntity.setErrorLog(e);
      manyManyResultEntity.setUserErrorLog("Error during proecessing request");
      manyManyRepository.saveAndFlush(manyManyResultEntity);
      e.printStackTrace();
    }
  }
}
