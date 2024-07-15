package pl.poznan.put.rnatangoengine.service.manyMany;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.definitions.CommonChainSequenceEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureModelResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableTaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableManyManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManyOutput;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManySubmitFormInput;
import pl.poznan.put.rnatangoengine.service.StructureModelService;
import pl.poznan.put.rnatangoengine.utils.ManyManyUtils;

@Service
public class ManyManyService {
  @Autowired StructureModelService structureModelService;
  @Autowired ManyManyTaskService manyManyTaskService;
  @Autowired ManyManyRepository manyManyRepository;
  @Autowired ManyManyUtils manyManyUtils;

  public ManyManySetFormResponse manyMany(MultipartFile file) {
    StructureModelEntity structureModelEntity;
    try {
      structureModelEntity =
          structureModelService.createInitalModelFromBytes(
              file.getBytes(), file.getOriginalFilename());
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during processing file");
    }
    ManyManyResultEntity manyManyResultEntity = manyManyTaskService.setTask(structureModelEntity);

    return ImmutableManyManySetFormResponse.builder()
        .taskHashId(manyManyResultEntity.getHashId().toString())
        .addAllModels(
            manyManyResultEntity.getModels().stream()
                .map(
                    (modelI) ->
                        ImmutableStructureModelResponse.builder()
                            .fileId(modelI.getHashId().toString())
                            .fileName(modelI.getFilename())
                            .sequence("")
                            .sourceSelection(
                                ImmutableSelection.builder()
                                    .from(
                                        modelI
                                            .getSourceSelection()
                                            .getConvertedToSelectionImmutable())
                                    .build())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }

  public ManyManySetFormResponse manyManyFormState(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public ManyManySetFormResponse manyManyFormAddModel(String taskId, MultipartFile file) {
    ManyManyResultEntity manyManyResultEntity;
    try {
      manyManyResultEntity =
          manyManyTaskService.addModel(
              file.getBytes(), file.getOriginalFilename(), UUID.fromString(taskId));
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during processing file");
    }
    return manyManyUtils.buildFormStateResponse(manyManyResultEntity);
  }

  public ManyManySetFormResponse manyManyFormRemoveModel(String taskId, String modelId) {
    ManyManyResultEntity _manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_manyManyResultEntity, null)
        || !_manyManyResultEntity.getStatus().equals(Status.SETTING)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not modify processed task");
    }
    try {
      manyManyTaskService.removeModel(UUID.fromString(modelId), UUID.fromString(taskId));
      return manyManyUtils.buildFormStateResponse(
          manyManyRepository.getByHashId(UUID.fromString(taskId)));
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during processing request");
    }
  }

  public TaskIdResponse manyMany(ManyManySubmitFormInput input) {
    ManyManyResultEntity _manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(input.taskHashId()));
    if (Objects.equals(_manyManyResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
    if (_manyManyResultEntity.getStatus() != Status.SETTING) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not modify processed task");
    }
    if (_manyManyResultEntity.getModels().size() < 2) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You need to add at least two models");
    }
    CommonChainSequenceEntity commonChain = null;
    for (CommonChainSequenceEntity localCommonChain : _manyManyResultEntity.getCommonSequences()) {
      if (localCommonChain.getChain() == input.chain()) {
        commonChain = localCommonChain;
        break;
      }
    }
    if (Objects.equals(commonChain, null) || commonChain.getSequence() == "") {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "There is no common sequence for given chain");
    }
    try {
      return ImmutableTaskIdResponse.builder()
          .taskId(
              manyManyTaskService
                  .submitTask(
                      UUID.fromString(input.taskHashId()),
                      input.angles(),
                      input.threshold(),
                      input.chain())
                  .getHashId()
                  .toString())
          .build();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task preparation failed");
    }
  }

  public StatusResponse manyManyStatus(String taskId) {
    try {
      ManyManyResultEntity _manyManyResultEntity =
          manyManyRepository.getByHashId(UUID.fromString(taskId));
      if (_manyManyResultEntity.getStatus().equals(Status.FAILED)) {
        return ImmutableStatusResponse.builder()
            .status(_manyManyResultEntity.getStatus())
            .error(_manyManyResultEntity.getUserErrorLog())
            .build();
      } else if (_manyManyResultEntity.getStatus().equals(Status.SUCCESS)) {
        return ImmutableStatusResponse.builder()
            .status(_manyManyResultEntity.getStatus())
            .resultUrl("/many-many/" + _manyManyResultEntity.getHashId().toString() + "/result")
            .build();
      } else {
        return ImmutableStatusResponse.builder().status(_manyManyResultEntity.getStatus()).build();
      }
    } catch (Exception e) {
      e.printStackTrace();

      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "could not find the task");
    }
  }

  public ManyManyOutput manyManyResult(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
