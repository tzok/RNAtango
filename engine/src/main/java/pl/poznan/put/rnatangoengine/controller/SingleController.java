package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.rnatangoengine.dto.SingleInput;
import pl.poznan.put.rnatangoengine.dto.SingleOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.service.SingleService;

@RestController
public class SingleController {
  private final SingleService singleService;

  @Autowired
  public SingleController(final SingleService singleService) {
    this.singleService = singleService;
  }

  @PostMapping("/single")
  public ResponseEntity<TaskIdResponse> single(@RequestBody SingleInput singleInput) {
    return new ResponseEntity<>(singleService.single(singleInput), HttpStatus.ACCEPTED);
  }

  @GetMapping("/single/{taskId}")
  public StatusResponse singleStatus(@PathVariable String taskId) {
    return singleService.singleStatus(taskId);
  }

  @GetMapping("/single/{taskId}/result")
  public SingleOutput singleResult(@PathVariable String taskId) {
    return singleService.singleResult(taskId);
  }
}
