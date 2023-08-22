package pl.poznan.put.rnatangoengine.controller;

import org.springframework.web.bind.annotation.*;
import pl.poznan.put.rnatangoengine.dto.SingleInput;
import pl.poznan.put.rnatangoengine.dto.SingleOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;

@RestController
public class Single {
  @PostMapping("/single")
  public TaskIdResponse single(@RequestBody SingleInput singleInput) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/single/{taskId}")
  public StatusResponse singleStatus(@PathVariable String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @GetMapping("/single/{taskId}/result")
  public SingleOutput singleResult(@PathVariable String taskId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
