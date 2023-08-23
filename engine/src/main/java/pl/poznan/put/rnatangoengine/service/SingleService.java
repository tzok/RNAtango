package pl.poznan.put.rnatangoengine.service;

import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.dto.SingleInput;
import pl.poznan.put.rnatangoengine.dto.SingleOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;

@Service
public class SingleService {
  public TaskIdResponse single(SingleInput singleInput) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public StatusResponse singleStatus(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public SingleOutput singleResult(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
