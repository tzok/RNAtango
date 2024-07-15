package pl.poznan.put.rnatangoengine.logic.manyManyProcessing;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelectionChain;
import pl.poznan.put.rnatangoengine.dto.Selection;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.logic.TargetModelsComparsionService;
import pl.poznan.put.rnatangoengine.service.StructureModelService;

@Service
public class ManyManyProcessing {

  @Autowired ManyManyRepository manyManyRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired SelectionChainRepository selectionChainRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureModelService structureModelService;
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
      if (selectionChainEntity.getName() == chain) {
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
          structureModelRepository.saveAndFlush(structureModelEntity);
          break;
        }
      }
    }
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
    } catch (Exception e) {
      manyManyResultEntity.setStatus(Status.FAILED);
      manyManyResultEntity.setErrorLog(e);
      manyManyResultEntity.setUserErrorLog("Error during proecessing request");
      manyManyRepository.saveAndFlush(manyManyResultEntity);
      e.printStackTrace();
    }
  }
}
