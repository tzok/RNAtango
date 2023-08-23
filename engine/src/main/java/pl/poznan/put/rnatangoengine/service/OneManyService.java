package pl.poznan.put.rnatangoengine.service;

import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.dto.OneManyInput;
import pl.poznan.put.rnatangoengine.dto.OneManyOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;

@Service
public class OneManyService {
  public TaskIdResponse oneMany(OneManyInput input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public StatusResponse oneManyStatus(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public OneManyOutput oneManyResult(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
