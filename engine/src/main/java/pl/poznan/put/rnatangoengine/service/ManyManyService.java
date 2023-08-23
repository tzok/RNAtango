package pl.poznan.put.rnatangoengine.service;

import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.dto.ManyManyInput;
import pl.poznan.put.rnatangoengine.dto.ManyManyOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;

@Service
public class ManyManyService {
  public TaskIdResponse manyMany(ManyManyInput input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public StatusResponse manyManyStatus(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public ManyManyOutput manyManyResult(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
