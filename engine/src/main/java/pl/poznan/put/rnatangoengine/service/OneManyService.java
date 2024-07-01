package pl.poznan.put.rnatangoengine.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.ImmutableOneManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelectionChain;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureModelResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureTargetResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableTaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.IndexPair;
import pl.poznan.put.rnatangoengine.dto.OneManyAddModelResponse;
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

@Service
public class OneManyService {
  @Autowired OneManyRepository oneManyRepository;
  @Autowired FileRepository fileRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired CompareStructures compareStructures;

  private static final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketHandler.class);

  public OneManySetFormResponse oneManyFormAddModel(String taskId, MultipartFile file) {
    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(taskId));
    if (_oneManyResultEntity.getModels().size() >= 10) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Number of models is limited to 10");
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

      // List<IndexPair> targetIndexPairs = new ArrayList<>();
      // for (StructureModelEntity structureModelEntity :
      // _oneManyResultEntity.getModels()) {
      // targetIndexPairs.add(new IndexPair(, toInclusive))
      // }

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
      selectionEntity = selectionRepository.saveAndFlush(selectionEntity);
      StructureModelEntity model =
          new StructureModelEntity(
              structureFilteredContent.getBytes(), file.getOriginalFilename(), selectionEntity);
      model.setTargetRangeRelative(
          new IndexPair(
              comparingResult.getTargetFromInclusiveRelative(),
              comparingResult.getTargetToInclusiveRelative()));
      model = structureModelRepository.saveAndFlush(model);
      _oneManyResultEntity.addModel(model);
      _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);
      return ImmutableOneManySetFormResponse.builder()
          .target(
              ImmutableStructureTargetResponse.builder()
                  .sequence(_oneManyResultEntity.getTargetEntity().getSourceSequence())
                  .sourceSelection(
                      ImmutableSelection.builder()
                          .from(
                              _oneManyResultEntity
                                  .getTargetEntity()
                                  .getTargetSourceSelection()
                                  .getConvertedToSelectionImmutable())
                          .build())
                  .selection(
                      ImmutableSelection.builder()
                          .from(
                              _oneManyResultEntity
                                  .getTargetEntity()
                                  .getSelection()
                                  .getConvertedToSelectionImmutable())
                          .build())
                  .build())
          .models(
              _oneManyResultEntity.getModels().stream()
                  .map(
                      (modelI) ->
                          ImmutableStructureModelResponse.builder()
                              .fileId(modelI.getHashId().toString())
                              .sequence(modelI.getFilteredSequence())
                              .originalSequence(modelI.getSourceSequence())
                              .selection(
                                  ImmutableSelection.builder()
                                      .from(
                                          modelI.getSelection().getConvertedToSelectionImmutable())
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();

    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      String sStackTrace = sw.toString(); // stack trace as a string
      LOGGER.error(sStackTrace);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Error during comparing model to target");
    }
  }

  public OneManyAddModelResponse oneManyFormRemoveModel(String taskId, String modelId) {

    throw new UnsupportedOperationException("Not implemented yet");

    // return ImmutableOneManyAddModelResponse.builder().build();
  }

  public TaskIdResponse oneMany(OneManySetFormInput input) {
    try {
      FileEntity targetFile = fileRepository.getByHashId(UUID.fromString(input.targetHashId()));
      SelectionEntity selection =
          selectionRepository.saveAndFlush(new SelectionEntity(input.selection()));
      StructureModelEntity target =
          structureModelRepository.saveAndFlush(new StructureModelEntity(targetFile, selection));
      OneManyResultEntity _oneManyResultEntity =
          oneManyRepository.saveAndFlush(new OneManyResultEntity(target, selection));
      // fileRepository.deleteByHashId(UUID.fromString(input.targetHashId()));

      Structure structure =
          structureProcessingService.process(
              new String(target.getSourceContent(), StandardCharsets.UTF_8), target.getFilename());
      target.setStructureMolecule(structure.getStructureMoleculeName());
      target.setSourceContent(structure.filterParseCif(input.selection(), false).getBytes());
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
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public OneManySetFormResponse oneManyFormState(String taskId) {
    try {
      OneManyResultEntity _oneManyResultEntity =
          oneManyRepository.getByHashId(UUID.fromString(taskId));

      return ImmutableOneManySetFormResponse.builder()
          .target(
              ImmutableStructureTargetResponse.builder()
                  .sequence(_oneManyResultEntity.getTargetEntity().getSourceSequence())
                  .sourceSelection(
                      ImmutableSelection.builder()
                          .from(
                              _oneManyResultEntity
                                  .getTargetEntity()
                                  .getTargetSourceSelection()
                                  .getConvertedToSelectionImmutable())
                          .build())
                  .selection(
                      ImmutableSelection.builder()
                          .from(
                              _oneManyResultEntity
                                  .getTargetEntity()
                                  .getSelection()
                                  .getConvertedToSelectionImmutable())
                          .build())
                  .build())
          .models(
              _oneManyResultEntity.getModels().stream()
                  .map(
                      (model) ->
                          ImmutableStructureModelResponse.builder()
                              .fileId(model.getHashId().toString())
                              .sequence(model.getFilteredSequence())
                              .originalSequence(model.getSourceSequence())
                              .selection(
                                  ImmutableSelection.builder()
                                      .from(model.getSelection().getConvertedToSelectionImmutable())
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
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
