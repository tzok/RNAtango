package pl.poznan.put.rnatangoengine.service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
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
import pl.poznan.put.rnatangoengine.dto.IndexPair;
import pl.poznan.put.rnatangoengine.dto.OneManyOutput;
import pl.poznan.put.rnatangoengine.dto.OneManySetFormInput;
import pl.poznan.put.rnatangoengine.dto.OneManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.OneManySubmitFormInput;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.StructureChainSequence;
import pl.poznan.put.rnatangoengine.dto.StructureComparingResult;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.logic.CompareStructures;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.logic.oneManyProcessing.OneManyProcessing;
import pl.poznan.put.rnatangoengine.utils.ModelTargetMatchingException;
import pl.poznan.put.rnatangoengine.utils.OneManyUtils;

@Service
public class OneManyService {
  @Autowired OneManyRepository oneManyRepository;
  @Autowired FileRepository fileRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired CompareStructures compareStructures;
  @Autowired OneManyUtils oneManyUtils;
  @Autowired OneManyProcessing oneManyProcessing;
  private static final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketHandler.class);

  public OneManySetFormResponse oneManyFormAddModel(String taskId, MultipartFile file) {
    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(taskId));
    if (_oneManyResultEntity.getStatus() != Status.SETTING) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not modify processed task");
    }
    if (_oneManyResultEntity.getModels().size() >= 10) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Number of models is limited to 10");
    }
    try {
      Structure structure =
          structureProcessingService.process(
              new String(file.getBytes(), StandardCharsets.UTF_8), file.getOriginalFilename());

      String structureFilteredContent =
          structure.filterParseCif("1", _oneManyResultEntity.getChain());

      StructureComparingResult comparingResult =
          compareStructures.compareTargetAndModelSequences(
              _oneManyResultEntity.getTargetEntity().getSourceSequence(),
              structure
                  .getContinuousSequences()
                  .getChainSubsequence(_oneManyResultEntity.getChain()));

      StructureChainSequence bestSubsequence = comparingResult.getModel();
      SelectionEntity selectionEntity =
          new SelectionEntity(
              ImmutableSelection.builder()
                  .modelName("1")
                  .addChains(
                      ImmutableSelectionChain.builder()
                          .name(_oneManyResultEntity.getChain())
                          .nucleotideRange(
                              ImmutableNucleotideRange.builder()
                                  .fromInclusive(
                                      bestSubsequence.getFrom()
                                          + comparingResult.getModelFromInclusiveRelative())
                                  .toInclusive(
                                      bestSubsequence.getFrom()
                                          + comparingResult.getModelToInclusiveRelative()
                                          + comparingResult.getModelFromInclusiveRelative())
                                  .build())
                          .build())
                  .build());
      SelectionEntity sourceSelectionEntity =
          new SelectionEntity(
              ImmutableSelection.builder()
                  .modelName("1")
                  .addChains(
                      ImmutableSelectionChain.builder()
                          .name(_oneManyResultEntity.getChain())
                          .nucleotideRange(
                              ImmutableNucleotideRange.builder()
                                  .fromInclusive(bestSubsequence.getFrom())
                                  .toInclusive(bestSubsequence.getTo())
                                  .build())
                          .build())
                  .build());
      selectionEntity = selectionRepository.saveAndFlush(selectionEntity);
      sourceSelectionEntity = selectionRepository.saveAndFlush(sourceSelectionEntity);
      StructureModelEntity model =
          new StructureModelEntity(
              structureFilteredContent.getBytes(),
              file.getOriginalFilename(),
              selectionEntity,
              sourceSelectionEntity);
      model.setTargetRangeRelative(
          new IndexPair(
              comparingResult.getTargetFromInclusiveRelative(),
              comparingResult.getTargetToInclusiveRelative()));
      model.setFilteredSequence(comparingResult.getSequence());
      model.setSourceSequence(bestSubsequence.getSequence());
      model.setStructureMolecule(structure.getStructureMoleculeName());
      model = structureModelRepository.saveAndFlush(model);
      _oneManyResultEntity.addModel(model);
      _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);

      oneManyUtils.applyCommonSubsequenceToTarget(_oneManyResultEntity);

      _oneManyResultEntity = oneManyRepository.getByHashId(UUID.fromString(taskId));

      return oneManyUtils.buildFormStateResponse(_oneManyResultEntity);

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
    if (_oneManyResultEntity.getStatus() != Status.SETTING) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not modify processed task");
    }
    _oneManyResultEntity = oneManyRepository.getByHashId(UUID.fromString(taskId));
    _oneManyResultEntity.removeModel(
        structureModelRepository.getByHashId(UUID.fromString(modelId)));
    _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);
    structureModelRepository.deleteByHashId(UUID.fromString(modelId));
    oneManyUtils.applyCommonSubsequenceToTarget(_oneManyResultEntity);

    return oneManyUtils.buildFormStateResponse(_oneManyResultEntity);
  }

  public TaskIdResponse oneMany(OneManySetFormInput input) {
    try {
      Structure structure = structureProcessingService.process(input.targetHashId());
      byte[] structureFilteredContent =
          structure.filterParseCif(input.selection(), false).getBytes();
      StructureChainSequence chainSequence =
          structure
              .getContinuousSequences()
              .getChainSubsequence(input.selection().chains().get(0).name())
              .get(0);
      SelectionEntity sourceSelection =
          selectionRepository.saveAndFlush(
              new SelectionEntity(
                  ImmutableSelection.builder()
                      .modelName(input.selection().modelName())
                      .addChains(
                          ImmutableSelectionChain.builder()
                              .name(input.selection().chains().get(0).name())
                              .nucleotideRange(
                                  ImmutableNucleotideRange.builder()
                                      .fromInclusive(chainSequence.getFrom())
                                      .toInclusive(chainSequence.getTo())
                                      .build())
                              .build())
                      .build()));
      SelectionEntity selection =
          selectionRepository.saveAndFlush(
              new SelectionEntity(
                  ImmutableSelection.builder()
                      .modelName(input.selection().modelName())
                      .addChains(
                          ImmutableSelectionChain.builder()
                              .name(input.selection().chains().get(0).name())
                              .nucleotideRange(
                                  ImmutableNucleotideRange.builder()
                                      .fromInclusive(chainSequence.getFrom())
                                      .toInclusive(chainSequence.getTo())
                                      .build())
                              .build())
                      .build()));
      StructureModelEntity target =
          structureModelRepository.saveAndFlush(
              new StructureModelEntity(
                  structureFilteredContent, structure.getStructureName(), sourceSelection));
      target.setSelection(selection);
      OneManyResultEntity _oneManyResultEntity =
          oneManyRepository.saveAndFlush(
              new OneManyResultEntity(
                  target,
                  selection.getModelName(),
                  selection.getSelectionChains().get(0).getName()));
      try {
        fileRepository.deleteByHashId(UUID.fromString(input.targetHashId()));
      } catch (Exception e) {
      }
      target.setStructureMolecule(structure.getStructureMoleculeName());
      target.setContent(structureFilteredContent);
      target.setSourceSequence(structure.getModelSequence());
      target.setFilteredSequence(structure.getModelSequence());
      target = structureModelRepository.saveAndFlush(target);
      _oneManyResultEntity.setTargetEntity(target);
      _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);
      return ImmutableTaskIdResponse.builder()
          .taskId(_oneManyResultEntity.getHashId().toString())
          .build();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not set the task");
    }
  }

  public TaskIdResponse oneMany(OneManySubmitFormInput input) {
    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(input.taskHashId()));

    // if (_oneManyResultEntity.getStatus() != Status.SETTING) {
    //   throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not modify processed task");
    // }
    _oneManyResultEntity.setTreshold(input.threshold());
    _oneManyResultEntity.setAnglesToAnalyze(input.angles());
    _oneManyResultEntity.setStatus(Status.WAITING);
    _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);
    try {
      oneManyProcessing.startTask(UUID.fromString(input.taskHashId()));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ImmutableTaskIdResponse.builder()
        .taskId(_oneManyResultEntity.getHashId().toString())
        .build();
  }

  public OneManySetFormResponse oneManyFormState(String taskId) {
    try {
      OneManyResultEntity _oneManyResultEntity =
          oneManyRepository.getByHashId(UUID.fromString(taskId));

      return oneManyUtils.buildFormStateResponse(_oneManyResultEntity);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exit");
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
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
