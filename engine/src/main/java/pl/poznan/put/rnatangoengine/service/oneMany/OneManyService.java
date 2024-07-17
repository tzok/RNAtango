package pl.poznan.put.rnatangoengine.service.oneMany;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelectionChain;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableTaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableTorsionAngleDifferences;
import pl.poznan.put.rnatangoengine.dto.Selection;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.oneMany.ImmutableOneManyOutput;
import pl.poznan.put.rnatangoengine.dto.oneMany.OneManyOutput;
import pl.poznan.put.rnatangoengine.dto.oneMany.OneManySetFormInput;
import pl.poznan.put.rnatangoengine.dto.oneMany.OneManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.oneMany.OneManySubmitFormInput;
import pl.poznan.put.rnatangoengine.logic.StructureLcs;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.logic.oneManyProcessing.OneManyProcessing;
import pl.poznan.put.rnatangoengine.service.StructureModelService;
import pl.poznan.put.rnatangoengine.utils.ModelTargetMatchingException;
import pl.poznan.put.rnatangoengine.utils.OneManyUtils;

@Service
public class OneManyService {
  @Autowired OneManyRepository oneManyRepository;
  @Autowired FileRepository fileRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired StructureLcs compareStructures;
  @Autowired OneManyUtils oneManyUtils;
  @Autowired OneManyProcessing oneManyProcessing;
  @Autowired OneManyTaskService oneManyTaskService;
  @Autowired StructureModelService structureModelService;

  public OneManySetFormResponse oneManyFormAddModel(String taskId, MultipartFile file) {
    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_oneManyResultEntity, null)
        || !_oneManyResultEntity.getStatus().equals(Status.SETTING)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not modify processed task");
    }
    if (_oneManyResultEntity.getModels().size() >= 10) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Number of models is limited to 10");
    }
    try {
      return oneManyUtils.buildFormStateResponse(
          oneManyTaskService.addModel(
              file.getBytes(), file.getOriginalFilename(), UUID.fromString(taskId)));
    } catch (ModelTargetMatchingException e) {
      throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No matching");
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Error during comparing model to target");
    }
  }

  @Transactional
  public OneManySetFormResponse oneManyFormRemoveModel(String taskId, String modelId) {

    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_oneManyResultEntity, null)
        || !_oneManyResultEntity.getStatus().equals(Status.SETTING)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not modify processed task");
    }
    try {
      return oneManyUtils.buildFormStateResponse(
          oneManyTaskService.removeModel(UUID.fromString(modelId), UUID.fromString(taskId)));
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during processing request");
    }
  }

  public TaskIdResponse oneMany(OneManySetFormInput input) {
    try {
      return ImmutableTaskIdResponse.builder()
          .taskId(
              oneManyTaskService
                  .setTask(
                      structureModelService.createModel(input.targetHashId(), input.selection()),
                      input.selection().modelName(),
                      input.selection().chains().get(0).name())
                  .getHashId()
                  .toString())
          .build();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not set the task");
    }
  }

  public TaskIdResponse oneMany(OneManySubmitFormInput input) {
    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(input.taskHashId()));
    if (Objects.equals(_oneManyResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
    if (_oneManyResultEntity.getStatus() != Status.SETTING) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not modify processed task");
    }
    if (_oneManyResultEntity.getModels().size() == 0) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You need to add at least one model");
    }

    try {
      return ImmutableTaskIdResponse.builder()
          .taskId(
              oneManyTaskService
                  .submitTask(
                      UUID.fromString(input.taskHashId()), input.angles(), input.threshold())
                  .getHashId()
                  .toString())
          .build();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task preparation failed");
    }
  }

  public OneManySetFormResponse oneManyFormState(String taskId) {
    try {
      OneManyResultEntity _oneManyResultEntity =
          oneManyRepository.getByHashId(UUID.fromString(taskId));
      if (Objects.equals(_oneManyResultEntity, null)
          || !_oneManyResultEntity.getStatus().equals(Status.SETTING)) {
        throw new Exception("task does not exist");
      }
      return oneManyUtils.buildFormStateResponse(_oneManyResultEntity);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
  }

  public StatusResponse oneManyStatus(String taskId) {
    try {
      OneManyResultEntity _oneManyResultEntity =
          oneManyRepository.getByHashId(UUID.fromString(taskId));
      if (_oneManyResultEntity.getStatus().equals(Status.FAILED)) {
        return ImmutableStatusResponse.builder()
            .status(_oneManyResultEntity.getStatus())
            .error(_oneManyResultEntity.getUserErrorLog())
            .build();
      } else if (_oneManyResultEntity.getStatus().equals(Status.SUCCESS)) {
        return ImmutableStatusResponse.builder()
            .status(_oneManyResultEntity.getStatus())
            .resultUrl("/one-many/" + _oneManyResultEntity.getHashId().toString() + "/result")
            .build();
      } else {
        return ImmutableStatusResponse.builder().status(_oneManyResultEntity.getStatus()).build();
      }
    } catch (Exception e) {
      e.printStackTrace();

      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "could not find the task");
    }
  }

  public OneManyOutput oneManyResult(String taskId) {
    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(taskId));
    if (_oneManyResultEntity != null && _oneManyResultEntity.getStatus().equals(Status.SUCCESS)) {
      return ImmutableOneManyOutput.builder()
          .model(_oneManyResultEntity.getModelNumber())
          .resultRemovedAfter(
              _oneManyResultEntity.getRemoveAfter() != null
                  ? new SimpleDateFormat("dd-MM-yyyy").format(_oneManyResultEntity.getRemoveAfter())
                  : "")
          .targetHashId(_oneManyResultEntity.getTargetEntity().getHashId().toString())
          .targetFileName(_oneManyResultEntity.getTargetEntity().getFilename())
          .addAllRequestedAngles(_oneManyResultEntity.getAnglesToAnalyze())
          .chain(_oneManyResultEntity.getChain())
          .lcsThreshold(_oneManyResultEntity.getThreshold())
          .addAllDifferences(
              _oneManyResultEntity.getModels().stream()
                  .map(
                      (model) ->
                          ImmutableTorsionAngleDifferences.builder()
                              .model("1")
                              .modelMCQ(model.getMcqValue())
                              .modelName(model.getFilename())
                              .modelHashId(model.getHashId().toString())
                              .addAllResidueMCQs(
                                  model.getTorsionAngleEntities().stream()
                                      .map((residue) -> residue.getMcqValue())
                                      .collect(Collectors.toList()))
                              .residues(
                                  model.getTorsionAngleEntities().stream()
                                      .map((residue) -> residue.getConvertedToResidueImmutable())
                                      .collect(Collectors.toList()))
                              .modelLCS(
                                  model.getLcsResult() != null
                                      ? model.getLcsResult().getConvertedToLCSImmutable()
                                      : null)
                              .build())
                  .collect(Collectors.toList()))
          .build();
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task not available");
    }
  }

  public byte[] oneManySecondaryStructureModel(String modelId) {
    StructureModelEntity structureModelEntity =
        structureModelRepository.getByHashId(UUID.fromString(modelId));
    if (structureModelEntity != null) {
      byte[] structureSVG = structureModelEntity.getSecondaryStructureVisualizationSVG();
      if (structureSVG.length == 0) {
        throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Model not available");
      }
      return structureSVG;

    } else {
      throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Model not available");
    }
  }

  public byte[] oneManyTertiaryStructureModel(String modelId) {
    StructureModelEntity structureModelEntity =
        structureModelRepository.getByHashId(UUID.fromString(modelId));
    if (structureModelEntity != null) {
      byte[] structureSVG = structureModelEntity.getContent();
      if (structureSVG.length == 0) {
        throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Model not available");
      }
      return structureSVG;
    } else {
      throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Model not available");
    }
  }

  public TaskIdResponse oneManyExample(String example) {
    Selection selection =
        ImmutableSelection.builder()
            .modelName("1")
            .addChains(
                ImmutableSelectionChain.builder()
                    .sequence("")
                    .name("A")
                    .nucleotideRange(
                        ImmutableNucleotideRange.builder().fromInclusive(0).toInclusive(70).build())
                    .build())
            .build();
    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      FileEntity _targetFileEntity;
      byte[] modelOne;
      String modelOneName;
      byte[] modelTwo;
      String modelTwoName;
      byte[] modelThree;
      String modelThreeName;

      switch (example) {
        case "1":
          _targetFileEntity =
              fileRepository.saveAndFlush(
                  new FileEntity(
                      "18_solution_0.pdb",
                      classloader.getResourceAsStream("18_solution_0.pdb").readAllBytes()));
          modelOne = classloader.getResourceAsStream("18_Szachniuk_1.pdb").readAllBytes();
          modelOneName = "18_Szachniuk_1.pdb";
          modelTwo = classloader.getResourceAsStream("18_Lee_1.pdb").readAllBytes();
          modelTwoName = "18_Lee_1.pdb";
          modelThree = classloader.getResourceAsStream("18_YagoubAli_1.pdb").readAllBytes();
          modelThreeName = "18_YagoubAli_1.pdb";

          break;
        case "2":
          _targetFileEntity =
              fileRepository.saveAndFlush(
                  new FileEntity(
                      "18_Ding_1.pdb",
                      classloader.getResourceAsStream("18_Ding_1.pdb").readAllBytes()));
          modelOne = classloader.getResourceAsStream("18_Chen_1.pdb").readAllBytes();
          modelOneName = "18_Chen_1.pdb";
          modelTwo = classloader.getResourceAsStream("18_Das_1.pdb").readAllBytes();
          modelTwoName = "18_Das_1.pdb";
          modelThree = classloader.getResourceAsStream("18_YagoubAli_1.pdb").readAllBytes();
          modelThreeName = "18_YagoubAli_1.pdb";
          break;
        case "3":
        default:
          _targetFileEntity =
              fileRepository.saveAndFlush(
                  new FileEntity(
                      "18_Chen_1.pdb",
                      classloader.getResourceAsStream("18_Chen_1.pdb").readAllBytes()));
          modelOne = classloader.getResourceAsStream("18_Szachniuk_1.pdb").readAllBytes();
          modelOneName = "18_Szachniuk_1.pdb";
          modelTwo = classloader.getResourceAsStream("18_Dokholyan_1.pdb").readAllBytes();
          modelTwoName = "18_Dokholyan_1.pdb";
          modelThree = classloader.getResourceAsStream("18_YagoubAli_1.pdb").readAllBytes();
          modelThreeName = "18_YagoubAli_1.pdb";
          break;
      }

      OneManyResultEntity oneManyResultEntity =
          oneManyTaskService.setTask(
              structureModelService.createModel(
                  _targetFileEntity.getHashId().toString(), selection),
              "1",
              "A");
      oneManyTaskService.addModel(modelOne, modelOneName, oneManyResultEntity.getHashId());
      oneManyTaskService.addModel(modelTwo, modelTwoName, oneManyResultEntity.getHashId());
      oneManyTaskService.addModel(modelThree, modelThreeName, oneManyResultEntity.getHashId());
      return ImmutableTaskIdResponse.builder()
          .taskId(oneManyResultEntity.getHashId().toString())
          .build();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not set the task");
    }
  }
}
