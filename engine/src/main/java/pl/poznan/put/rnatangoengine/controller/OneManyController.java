package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.poznan.put.rnatangoengine.dto.OneManyOutput;
import pl.poznan.put.rnatangoengine.dto.OneManySetFormInput;
import pl.poznan.put.rnatangoengine.dto.OneManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.OneManySubmitFormInput;
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

  @PostMapping("/one-many/set")
  public ResponseEntity<TaskIdResponse> oneManySetForm(@RequestBody OneManySetFormInput input) {
    return new ResponseEntity<>(oneManyService.oneMany(input), HttpStatus.ACCEPTED);
  }

  @GetMapping("/one-many/form/{taskId}")
  public ResponseEntity<OneManySetFormResponse> oneManyFormState(
      @RequestBody @PathVariable String taskId) {
    return new ResponseEntity<>(oneManyService.oneManyFormState(taskId), HttpStatus.ACCEPTED);
  }

  @PostMapping(
      path = "/one-many/form/add/model",
      consumes = {"multipart/form-data"})
  public ResponseEntity<OneManySetFormResponse> oneManySubmitFormAddModel(
      @RequestParam("taskId") String taskId, @RequestParam("file") MultipartFile file) {
    return new ResponseEntity<>(
        oneManyService.oneManyFormAddModel(taskId, file), HttpStatus.ACCEPTED);
  }

  @GetMapping("/one-many/form/remove/model/{taskId}/{modelId}")
  public ResponseEntity<OneManySetFormResponse> oneManySubmitFormRemoveModel(
      @PathVariable String taskId, @PathVariable String modelId) {
    return new ResponseEntity<>(
        oneManyService.oneManyFormRemoveModel(taskId, modelId), HttpStatus.ACCEPTED);
  }

  @PostMapping("/one-many/submit")
  public ResponseEntity<TaskIdResponse> oneManySubmitForm(
      @RequestBody OneManySubmitFormInput input) {
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
