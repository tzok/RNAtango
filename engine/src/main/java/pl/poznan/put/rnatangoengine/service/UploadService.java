package pl.poznan.put.rnatangoengine.service;

import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.StructureFileInput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;

@Service
public class UploadService {
  public TaskIdResponse upload(StructureFileInput structureFileInput) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public StatusResponse uploadStatus(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public StructureFileOutput uploadResult(String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
