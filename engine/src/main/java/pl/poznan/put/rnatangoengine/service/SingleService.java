package pl.poznan.put.rnatangoengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableSingleOutput;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableTaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.SingleInput;
import pl.poznan.put.rnatangoengine.dto.SingleOutput;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.logic.singleProcessing.SingleProcessing;

@Service
public class SingleService {
  @Autowired SingleResultRepository singleRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired FileRepository fileRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired SingleProcessing singleProcessing;
  QueueService queueService;

  public TaskIdResponse single(SingleInput singleInput) {
    try {
      Structure structure = structureProcessingService.process(singleInput.fileId());
      List<SelectionEntity> selections = new ArrayList<>();
      selections.addAll(
          singleInput.selections().stream()
              .map((selection) -> new SelectionEntity(selection))
              .collect(Collectors.toList()));
      selectionRepository.saveAll(selections);

      SingleResultEntity _singleResultEntity =
          singleRepository.saveAndFlush(
              new SingleResultEntity(
                  selections, structure.filterParseCif(singleInput.selections()).getBytes()));

      // queueService.send(_singleResultEntity.getHashId().toString(), TaskType.Single);
      fileRepository.deleteByHashId(UUID.fromString(singleInput.fileId()));
      singleProcessing.startTask(_singleResultEntity.getHashId());
      return ImmutableTaskIdResponse.builder()
          .taskId(_singleResultEntity.getHashId().toString())
          .build();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not set the task");
    }
  }

  public StatusResponse singleStatus(String taskId) {
    try {
      SingleResultEntity _singleResultEntity =
          singleRepository.getByHashId(UUID.fromString(taskId));
      if (_singleResultEntity.getStatus().equals(Status.SUCCESS)) {
        return ImmutableStatusResponse.builder()
            .status(_singleResultEntity.getStatus())
            .resultUrl("/single/" + _singleResultEntity.getHashId().toString() + "/result")
            .build();
      } else {
        return ImmutableStatusResponse.builder()
            .status(_singleResultEntity.getStatus())
            .resultUrl("")
            .build();
      }
    } catch (Exception e) {
      e.printStackTrace();

      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "could not find the task");
    }
  }

  public SingleOutput singleResult(String taskId) {
    SingleResultEntity _singleResultEntity = singleRepository.getByHashId(UUID.fromString(taskId));
    if (_singleResultEntity.getStatus().equals(Status.SUCCESS)) {
      return ImmutableSingleOutput.builder()
          .addAllTorsionAngles(
              _singleResultEntity.getChainTorsionAngles().stream()
                  .map((chainAngles) -> chainAngles.getConvertedToTorsionAnglesInChainImmutable())
                  .collect(Collectors.toList()))
          .build();
    } else {
      throw new ResponseStatusException(HttpStatus.LOCKED, "Not ready yet");
    }
  }
}
