package pl.poznan.put.rnatangoengine.controller;

import org.springframework.web.bind.annotation.*;
import pl.poznan.put.rnatangoengine.dto.OneManyInput;
import pl.poznan.put.rnatangoengine.dto.OneManyOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;

@RestController
public class OneMany {
  @PostMapping("/one-many")
  public TaskIdResponse oneMany(@RequestBody OneManyInput input) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/one-many/{taskId}")
  public StatusResponse oneManyStatus(@PathVariable String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/one-many/{taskId}/result")
  public OneManyOutput oneManyResult(@PathVariable String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
