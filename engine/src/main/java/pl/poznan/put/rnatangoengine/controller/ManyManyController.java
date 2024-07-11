package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManyOutput;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManySubmitFormInput;
import pl.poznan.put.rnatangoengine.service.manyMany.ManyManyService;

@RestController
public class ManyManyController {
  private final ManyManyService manyManyService;

  @Autowired
  public ManyManyController(final ManyManyService manyManyService) {
    this.manyManyService = manyManyService;
  }

  @PostMapping(
      path = "/many-many/set",
      consumes = {"multipart/form-data"})
  public ResponseEntity<ManyManySetFormResponse> manyManySetForm(
      @RequestParam("file") MultipartFile file) {
    return new ResponseEntity<>(manyManyService.manyMany(file), HttpStatus.ACCEPTED);
  }

  @GetMapping("/many-many/form/{taskId}")
  public ResponseEntity<ManyManySetFormResponse> manyManyFormState(
      @RequestBody @PathVariable String taskId) {
    return new ResponseEntity<>(manyManyService.manyManyFormState(taskId), HttpStatus.ACCEPTED);
  }

  @PostMapping(
      path = "/many-many/form/add/model/{taskId}",
      consumes = {"multipart/form-data"})
  public ResponseEntity<ManyManySetFormResponse> manyManySubmitFormAddModel(
      @PathVariable("taskId") String taskId, @RequestParam("file") MultipartFile file) {
    return new ResponseEntity<>(
        manyManyService.manyManyFormAddModel(taskId, file), HttpStatus.ACCEPTED);
  }

  @GetMapping("/many-many/form/remove/model/{taskId}/{modelId}")
  public ResponseEntity<ManyManySetFormResponse> manyManySubmitFormRemoveModel(
      @PathVariable String taskId, @PathVariable String modelId) {
    return new ResponseEntity<>(
        manyManyService.manyManyFormRemoveModel(taskId, modelId), HttpStatus.ACCEPTED);
  }

  @PostMapping("/many-many/submit")
  public ResponseEntity<TaskIdResponse> oneManySubmitForm(
      @RequestBody ManyManySubmitFormInput input) {
    return new ResponseEntity<>(manyManyService.manyMany(input), HttpStatus.ACCEPTED);
  }

  @GetMapping("/many-many/{taskId}")
  public StatusResponse oneManyStatus(@PathVariable String taskId) {
    return manyManyService.manyManyStatus(taskId);
  }

  @GetMapping("/many-many/{taskId}/result")
  public ManyManyOutput oneManyResult(@PathVariable String taskId) {
    return manyManyService.manyManyResult(taskId);
  }
}
