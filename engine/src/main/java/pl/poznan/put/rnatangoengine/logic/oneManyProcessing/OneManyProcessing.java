package pl.poznan.put.rnatangoengine.logic.oneManyProcessing;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.rnatangoengine.WebPushService;
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
  @Autowired WebPushService webPushService;

  public void startTask(UUID taskHashId) {

    OneManyResultEntity oneManyResultEntity = oneManyRepository.getByHashId(taskHashId);
    if (!oneManyResultEntity.getStatus().equals(Status.WAITING)) {
      return;
    }
    oneManyResultEntity.setStatus(Status.PROCESSING);
    oneManyRepository.save(oneManyResultEntity);
    try {
      process(oneManyResultEntity);
    } catch (Exception e) {
    }
  }

  public void process(OneManyResultEntity oneManyResultEntity) throws Exception {
    UUID hashId = oneManyRepository.saveAndFlush(oneManyResultEntity).getHashId();

    try {
      oneManyResultEntity = oneManyRepository.getByHashId(hashId);
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

      oneManyResultEntity = oneManyRepository.getByHashId(hashId);
      for (int i = 0; i < models.size(); i++) {
        StructureModelEntity model = oneManyResultEntity.getModels().get(i);
        model.setLcsResult(
            targetModelsComparsionService.lcs(
                target,
                oneManyResultEntity.getModels().get(i),
                oneManyResultEntity.getThreshold()));
        structureModelRepository.saveAndFlush(model);
        oneManyResultEntity.incrementProgress();
        oneManyResultEntity = oneManyRepository.saveAndFlush(oneManyResultEntity);
      }

      oneManyResultEntity.setFinalSequence(target.getFilteredSequence());
      oneManyResultEntity.setFinalStructure(
          comparisonResult.fragmentMatches().get(0).getTargetDotBracket().structure());
      oneManyResultEntity.setStatus(Status.SUCCESS);
      oneManyResultEntity
          .getSubscibers()
          .forEach(
              (s) ->
                  webPushService.sendNotificationToClient(
                      s, "Target vs models task " + hashId + " completed"));
      oneManyRepository.saveAndFlush(oneManyResultEntity);

    } catch (Exception e) {
      oneManyResultEntity.setStatus(Status.FAILED);
      oneManyResultEntity.setErrorLog(e);
      oneManyResultEntity.setUserErrorLog("Error during proecessing request");
      oneManyRepository.saveAndFlush(oneManyResultEntity);
      e.printStackTrace();
      oneManyResultEntity
          .getSubscibers()
          .forEach(
              (s) ->
                  webPushService.sendNotificationToClient(
                      s, "Target vs models task " + hashId + " processing failed"));
      throw new Exception("OneManyProcessing error");
    }
  }
}
