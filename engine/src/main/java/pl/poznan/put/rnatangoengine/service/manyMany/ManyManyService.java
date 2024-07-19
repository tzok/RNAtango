package pl.poznan.put.rnatangoengine.service.manyMany;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
import pl.poznan.put.rnatangoengine.database.repository.ClusteringResultRepository;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureModelResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableTaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableTorsionAngleDifferences;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ClusteringResult;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableManyManyOneInstance;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableManyManyOutput;
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
  @Autowired ClusteringResultRepository clusteringResultRepository;

  public List<ClusteringResult> manyManyClusteringResult(String taskId) {
    ManyManyResultEntity manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(manyManyResultEntity, null)
        || !manyManyResultEntity.getStatus().equals(Status.SUCCESS)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
    }
    return manyManyResultEntity.getClustering().stream()
        .map((cluster) -> cluster.getClusteringResultImmutable())
        .collect(Collectors.toList());
  }

  public byte[] manyManyDendrogram(String taskId) {
    ManyManyResultEntity manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(manyManyResultEntity, null)
        || !manyManyResultEntity.getStatus().equals(Status.SUCCESS)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
    }
    if (manyManyResultEntity.getDendrogram().length == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "");
    }
    return manyManyResultEntity.getDendrogram();
  }

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
    ManyManyResultEntity manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(manyManyResultEntity, null)
        || !manyManyResultEntity.getStatus().equals(Status.SETTING)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not modify processed task");
    }
    if (manyManyResultEntity.getModels().size() > 10) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Number of models is limited to 10");
    }

    return manyManyUtils.buildFormStateResponse(manyManyResultEntity);
  }

  public ManyManySetFormResponse manyManyFormAddModel(String taskId, MultipartFile file) {
    ManyManyResultEntity manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(manyManyResultEntity, null)
        || !manyManyResultEntity.getStatus().equals(Status.SETTING)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not modify processed task");
    }
    if (manyManyResultEntity.getModels().size() >= 10) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Number of models is limited to 10");
    }
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
    if (_manyManyResultEntity.getModels().size() < 3) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You need to add at least three models");
    }
    CommonChainSequenceEntity commonChain = null;
    for (CommonChainSequenceEntity localCommonChain : _manyManyResultEntity.getCommonSequences()) {
      if (localCommonChain.getChain().equals(input.chain())) {
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
      e.printStackTrace();
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
    ManyManyResultEntity _manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (_manyManyResultEntity != null && _manyManyResultEntity.getStatus().equals(Status.SUCCESS)) {
      return ImmutableManyManyOutput.builder()
          .model("1")
          .requestedAngles(_manyManyResultEntity.getAnglesToAnalyze())
          .resultRemovedAfter(
              _manyManyResultEntity.getRemoveAfter() != null
                  ? new SimpleDateFormat("dd-MM-yyyy")
                      .format(_manyManyResultEntity.getRemoveAfter())
                  : "")
          .chain(_manyManyResultEntity.getChainToAnalyze())
          .addAllStructureModels(
              _manyManyResultEntity.getModels().stream()
                  .map((model) -> model.getFilename())
                  .collect(Collectors.toList()))
          .addAllOneManyResults(
              _manyManyResultEntity.getAllComparations().stream()
                  .map(
                      (_oneManyResultEntity) ->
                          ImmutableManyManyOneInstance.builder()
                              .targetHashId(
                                  _oneManyResultEntity.getTargetEntity().getHashId().toString())
                              .targetFileName(_oneManyResultEntity.getTargetEntity().getFilename())
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
                                                          .map(
                                                              (residue) ->
                                                                  residue
                                                                      .getConvertedToResidueImmutable())
                                                          .collect(Collectors.toList()))
                                                  .modelLCS(
                                                      Objects.equals(model.getLcsResult(), null)
                                                          ? null
                                                          : model
                                                              .getLcsResult()
                                                              .getConvertedToLCSImmutable())
                                                  .build())
                                      .collect(Collectors.toList()))
                              .build())
                  .collect(Collectors.toList()))
          .build();

    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task not available");
    }
  }

  public TaskIdResponse manyManyExample(String example) {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    try {
      List<String> examples = new ArrayList<>();
      List<String> examplesNames = new ArrayList<>();

      switch (example) {
        case "1":
          examples.addAll(
              List.of(
                  "18_solution_0.pdb", "18_Szachniuk_1.pdb", "18_Lee_1.pdb", "18_YagoubAli_1.pdb"));
          examplesNames.addAll(
              List.of(
                  "PZ18_model00.pdb", "PZ18_model01.pdb", "PZ18_model02.pdb", "PZ18_model03.pdb"));
          break;
        case "2":
          examples.addAll(
              List.of(
                  "manyMany/1a9nR.pdb",
                  "manyMany/1a9nR_M1.pdb",
                  "manyMany/1a9nR_M2.pdb",
                  "manyMany/1a9nR_M3.pdb",
                  "manyMany/1a9nR_M4.pdb",
                  "manyMany/1a9nR_M5.pdb",
                  "manyMany/1a9nR_M6.pdb",
                  "manyMany/1a9nR_M7.pdb",
                  "manyMany/1a9nR_M8.pdb",
                  "manyMany/1a9nR_M9.pdb"));
          examplesNames.addAll(
              List.of(
                  "1a9nR.pdb",
                  "1a9nR_M1.pdb",
                  "1a9nR_M2.pdb",
                  "1a9nR_M3.pdb",
                  "1a9nR_M4.pdb",
                  "1a9nR_M5.pdb",
                  "1a9nR_M6.pdb",
                  "1a9nR_M7.pdb",
                  "1a9nR_M8.pdb",
                  "1a9nR_M9.pdb"));
          break;
        case "3":
        default:
          examples.addAll(
              List.of(
                  "18_Chen_1.pdb",
                  "18_Szachniuk_1.pdb",
                  "18_Dokholyan_1.pdb",
                  "18_YagoubAli_1.pdb"));
          examplesNames.addAll(
              List.of(
                  "PZ18_model05.pdb", "PZ18_model01.pdb", "PZ18_model07.pdb", "PZ18_model03.pdb"));
          break;
      }
      StructureModelEntity structureModelEntity;
      structureModelEntity =
          structureModelService.createInitalModelFromBytes(
              classloader.getResourceAsStream(examples.get(0)).readAllBytes(),
              examplesNames.get(0));

      ManyManyResultEntity manyManyResultEntity = manyManyTaskService.setTask(structureModelEntity);

      for (int i = 1; i < examples.size(); i++) {
        manyManyTaskService.addModel(
            classloader.getResourceAsStream(examples.get(i)).readAllBytes(),
            examplesNames.get(i),
            manyManyResultEntity.getHashId());
      }
      return ImmutableTaskIdResponse.builder()
          .taskId(manyManyResultEntity.getHashId().toString())
          .build();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not set the task");
    }
  }
}
