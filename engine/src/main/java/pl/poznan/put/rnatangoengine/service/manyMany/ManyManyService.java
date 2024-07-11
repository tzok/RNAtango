package pl.poznan.put.rnatangoengine.service.manyMany;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureModelResponse;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableLongestChainSequence;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableManyManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManyOutput;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManySubmitFormInput;
import pl.poznan.put.rnatangoengine.service.StructureModelService;

@Service
public class ManyManyService {
  @Autowired StructureModelService structureModelService;
  @Autowired ManyManyTaskService manyManyTaskService;
  @Autowired ManyManyRepository manyManyRepository;

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
    StructureModelEntity structureModelEntity;
    try {
      structureModelEntity =
          structureModelService.createInitalModelFromBytes(
              file.getBytes(), file.getOriginalFilename());
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during processing file");
    }
    ManyManyResultEntity manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(manyManyResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during processing file");
    }

    manyManyResultEntity.addModel(structureModelEntity);
    manyManyResultEntity = manyManyRepository.saveAndFlush(manyManyResultEntity);
    manyManyTaskService.calculateCommonChainSequeces(manyManyResultEntity);
    manyManyResultEntity = manyManyRepository.getByHashId(UUID.fromString(taskId));
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
        .addAllSequences(
            manyManyResultEntity.getCommonSequences().stream()
                .map(
                    s ->
                        ImmutableLongestChainSequence.builder()
                            .sequence(s.getSequence())
                            .name(s.getChain())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }

  public ManyManySetFormResponse manyManyFormRemoveModel(String taskId, String modelId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public TaskIdResponse manyMany(ManyManySubmitFormInput input) {
    throw new UnsupportedOperationException("Not implemented yet");
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
