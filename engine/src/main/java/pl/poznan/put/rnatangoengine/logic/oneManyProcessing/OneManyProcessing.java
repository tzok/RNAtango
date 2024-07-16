package pl.poznan.put.rnatangoengine.logic.oneManyProcessing;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.logic.TargetModelsComparsionService;
import pl.poznan.put.rnatangoengine.service.StructureModelService;

@Service
public class OneManyProcessing {

  @Autowired OneManyRepository oneManyRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired SelectionChainRepository selectionChainRepository;
  @Autowired StructureModelService structureModelService;
  @Autowired TargetModelsComparsionService targetModelsComparsionService;

  public void startTask(UUID taskHashId) {

    OneManyResultEntity oneManyResultEntity = oneManyRepository.getByHashId(taskHashId);
    if (!oneManyResultEntity.getStatus().equals(Status.WAITING)) {
      return;
    }
    oneManyResultEntity.setStatus(Status.PROCESSING);
    oneManyRepository.save(oneManyResultEntity);

    process(oneManyResultEntity);
  }

  public void process(OneManyResultEntity oneManyResultEntity) {
    try {
      StructureModelEntity target =
          structureModelService.filterModelContent(oneManyResultEntity.getTargetEntity());
      List<StructureModelEntity> models =
          structureModelService.intersectModelsSelectionWithTarget(
              oneManyResultEntity.getModels(), target.getFilteredSequence());

      for (int i = 0; i < models.size(); i++) {
        models.set(i, structureModelService.filterModelContent(models.get(i)));
      }

      ModelsComparisonResult comparisonResult =
          targetModelsComparsionService.compare(
              target, models, oneManyResultEntity.getAnglesToAnalyze());

      oneManyResultEntity = oneManyRepository.getByHashId(oneManyResultEntity.getHashId());
      models = oneManyResultEntity.getModels();
      for (int i = 0; i < models.size(); i++) {
        StructureModelEntity model = models.get(i);
        model.setLcsResult(
            targetModelsComparsionService.lcs(
                target, models.get(i), oneManyResultEntity.getThreshold()));
        structureModelRepository.saveAndFlush(model);
      }

      oneManyResultEntity.setFinalSequence(target.getFilteredSequence());
      oneManyResultEntity.setFinalStructure(
          comparisonResult.fragmentMatches().get(0).getTargetDotBracket().structure());
      oneManyResultEntity.setStatus(Status.SUCCESS);
      oneManyRepository.saveAndFlush(oneManyResultEntity);
    } catch (Exception e) {
      oneManyResultEntity.setStatus(Status.FAILED);
      oneManyResultEntity.setErrorLog(e);
      oneManyResultEntity.setUserErrorLog("Error during proecessing request");
      oneManyRepository.saveAndFlush(oneManyResultEntity);
      e.printStackTrace();
    }
  }
}
