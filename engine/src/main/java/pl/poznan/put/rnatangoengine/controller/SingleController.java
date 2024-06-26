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

  @CrossOrigin(origins = "*")
  @PostMapping("/single")
  public ResponseEntity<TaskIdResponse> single(@RequestBody SingleInput singleInput) {
    return new ResponseEntity<>(singleService.single(singleInput), HttpStatus.ACCEPTED);
  }

  @CrossOrigin(origins = "*")
  @GetMapping("/single/{taskId}")
  public ResponseEntity<StatusResponse> singleStatus(@PathVariable String taskId) {
    return new ResponseEntity<>(singleService.singleStatus(taskId), HttpStatus.ACCEPTED);
  }

  @CrossOrigin(origins = "*")
  @GetMapping("/single/{taskId}/result")
  public ResponseEntity<SingleOutput> singleResult(@PathVariable String taskId) {
    return new ResponseEntity<>(singleService.singleResult(taskId), HttpStatus.ACCEPTED);
  }
}
