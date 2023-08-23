package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.rnatangoengine.dto.OneManyInput;
import pl.poznan.put.rnatangoengine.dto.OneManyOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.service.OneManyService;

@RestController
public class OneManyController {
  private final OneManyService oneManyService;

  @Autowired
  public OneManyController(final OneManyService oneManyService) {
    this.oneManyService = oneManyService;
  }

  @PostMapping("/one-many")
  public ResponseEntity<TaskIdResponse> oneMany(@RequestBody OneManyInput input) {
    return new ResponseEntity<>(oneManyService.oneMany(input), HttpStatus.ACCEPTED);
  }

  @GetMapping("/one-many/{taskId}")
  public StatusResponse oneManyStatus(@PathVariable String taskId) {
    return oneManyService.oneManyStatus(taskId);
  }

  @GetMapping("/one-many/{taskId}/result")
  public OneManyOutput oneManyResult(@PathVariable String taskId) {
    return oneManyService.oneManyResult(taskId);
  }
}
