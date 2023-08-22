package pl.poznan.put.rnatangoengine.controller;

import org.springframework.web.bind.annotation.*;
import pl.poznan.put.rnatangoengine.dto.ManyManyInput;
import pl.poznan.put.rnatangoengine.dto.ManyManyOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;

@RestController
public class ManyMany {
  @PostMapping("/many-many")
  public TaskIdResponse manyMany(@RequestBody ManyManyInput input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/many-many/{taskId}")
  public StatusResponse manyManyStatus(@PathVariable String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/many-many/{taskId}/result")
  public ManyManyOutput manyManyResult(@PathVariable String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
