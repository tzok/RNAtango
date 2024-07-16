package pl.poznan.put.rnatangoengine.logic.manyManyProcessing;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
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
                targetDetached, "1", targetDetached.getSelection().getModelName()));

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
        structureModelService.filterModelContent(structureModelEntity);
      }
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
      manyManyRepository.saveAndFlush(manyManyResultEntity);
      for (OneManyResultEntity oneManyResultEntity : manyManyResultEntity.getAllComparations()) {
        oneManyProcessing.process(oneManyResultEntity);
      }
      manyManyResultEntity.setStatus(Status.SUCCESS);
      manyManyRepository.saveAndFlush(manyManyResultEntity);

    } catch (Exception e) {
      manyManyResultEntity.setStatus(Status.FAILED);
      manyManyResultEntity.setErrorLog(e);
      manyManyResultEntity.setUserErrorLog("Error during proecessing request");
      manyManyRepository.saveAndFlush(manyManyResultEntity);
      e.printStackTrace();
    }
  }
}
