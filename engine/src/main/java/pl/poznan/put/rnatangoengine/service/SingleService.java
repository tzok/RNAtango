package pl.poznan.put.rnatangoengine.service;

import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.TaskType;
import pl.poznan.put.rnatangoengine.logic.Structure;

@Service
public class SingleService {
  @Autowired SingleResultRepository singleRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired FileRepository fileRepository;

  QueueService queueService;

  public TaskIdResponse single(SingleInput singleInput) {
    try {
      Structure structure = new Structure(singleInput.fileHashId());
      SelectionEntity _selectionEntity =
          selectionRepository.save(new SelectionEntity(singleInput.selection()));
      SingleResultEntity _singleResultEntity =
          singleRepository.saveAndFlush(
              new SingleResultEntity(_selectionEntity, structure.filter(singleInput.selection())));

      queueService.send(_singleResultEntity.getHashId().toString(), TaskType.Single);
      fileRepository.deleteByHashId(UUID.fromString(singleInput.fileHashId()));
      return ImmutableTaskIdResponse.builder()
          .taskId(_singleResultEntity.getHashId().toString())
          .build();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not set the task");
    }
  }

  public StatusResponse singleStatus(String taskId) {
    try {
      SingleResultEntity _singleResultEntity =
          singleRepository.getByHashId(UUID.fromString(taskId));
      return ImmutableStatusResponse.builder().status(_singleResultEntity.getStatus()).build();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "could not find the task");
    }
  }

  public SingleOutput singleResult(String taskId) {
    try {
      SingleResultEntity _singleResultEntity =
          singleRepository.getByHashId(UUID.fromString(taskId));
      return ImmutableSingleOutput.builder()
          .addAllTorsionAngles(
              _singleResultEntity.getChainTorsionAngles().stream()
                  .map((chainAngles) -> chainAngles.getConvertedToTorsionAnglesInChainImmutable())
                  .collect(Collectors.toList()))
          .build();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "could not find the task");
    }
  }
}
