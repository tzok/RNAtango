package pl.poznan.put.rnatangoengine.service.oneMany;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.Angle;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.logic.StructureLcs;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.logic.oneManyProcessing.OneManyProcessing;
import pl.poznan.put.rnatangoengine.service.QueueService;
import pl.poznan.put.rnatangoengine.service.StructureModelService;
import pl.poznan.put.rnatangoengine.utils.ModelTargetMatchingException;
import pl.poznan.put.rnatangoengine.utils.OneManyUtils;

@Service
public class OneManyTaskService {
  @Autowired OneManyRepository oneManyRepository;
  @Autowired FileRepository fileRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired StructureLcs structureLcs;
  @Autowired OneManyUtils oneManyUtils;
  @Autowired OneManyProcessing oneManyProcessing;
  @Autowired QueueService queueService;
  @Autowired StructureModelService structureModelService;

  public OneManyResultEntity submitTask(
      UUID oneManyEntityHashId, List<Angle> angles, Double threshold) throws Exception {
    OneManyResultEntity _oneManyResultEntity = oneManyRepository.getByHashId(oneManyEntityHashId);
    if (_oneManyResultEntity.equals(null)) {
      throw new Exception("task does not exist");
    }
    _oneManyResultEntity.setThreshold(threshold);
    _oneManyResultEntity.setAnglesToAnalyze(angles);
    _oneManyResultEntity.setStatus(Status.WAITING);
    _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);

    try {
      queueService.sendOneMany(_oneManyResultEntity.getHashId());
    } catch (Exception e) {
      _oneManyResultEntity.setStatus(Status.FAILED);
      _oneManyResultEntity.setUserErrorLog("Error during setting task");
      _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);

      e.printStackTrace();
    }
    return _oneManyResultEntity;
  }

  public OneManyResultEntity setTask(
      StructureModelEntity target, String modelNumber, String chain) {

    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.saveAndFlush(new OneManyResultEntity(target, modelNumber, chain));

    target = structureModelRepository.saveAndFlush(target);
    _oneManyResultEntity.setTargetEntity(target);
    return oneManyRepository.saveAndFlush(_oneManyResultEntity);
  }

  public OneManyResultEntity addModel(byte[] content, String filename, UUID oneManyEntityHashId)
      throws Exception, ModelTargetMatchingException {
    OneManyResultEntity _oneManyResultEntity = oneManyRepository.getByHashId(oneManyEntityHashId);
    if (_oneManyResultEntity == null) {
      throw new Exception("task does not exist");
    }
    StructureModelEntity model =
        structureModelService.createModelFromBytes(
            content, filename, _oneManyResultEntity.getChain());

    _oneManyResultEntity.addModel(
        structureModelService.applyModelTargetCommonSequence(
            model, _oneManyResultEntity.getTargetEntity().getSourceSequence()));
    _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);
    return oneManyUtils.applyCommonSubsequenceToTarget(_oneManyResultEntity);
  }

  public OneManyResultEntity removeModel(UUID modelhashId, UUID oneManyEntityHashId)
      throws Exception {
    OneManyResultEntity _oneManyResultEntity = oneManyRepository.getByHashId(oneManyEntityHashId);
    if (Objects.equals(_oneManyResultEntity, null)) {
      throw new Exception("task does not exist");
    }
    _oneManyResultEntity.removeModel(structureModelRepository.getByHashId(modelhashId));
    _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);
    structureModelRepository.deleteByHashId(modelhashId);
    return oneManyUtils.applyCommonSubsequenceToTarget(_oneManyResultEntity);
  }
}
