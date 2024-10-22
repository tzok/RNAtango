package pl.poznan.put.rnatangoengine.controller;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.oneMany.OneManyOutput;
import pl.poznan.put.rnatangoengine.dto.oneMany.OneManySetFormInput;
import pl.poznan.put.rnatangoengine.dto.oneMany.OneManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.oneMany.OneManySubmitFormInput;
import pl.poznan.put.rnatangoengine.service.oneMany.OneManyService;

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
      path = "/one-many/form/add/model/{taskId}",
      consumes = {"multipart/form-data"})
  public ResponseEntity<OneManySetFormResponse> oneManySubmitFormAddModel(
      @PathVariable("taskId") String taskId, @RequestParam("file") MultipartFile file) {
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
  public ResponseEntity<StatusResponse> oneManyStatus(@PathVariable String taskId) {
    return new ResponseEntity<>(oneManyService.oneManyStatus(taskId), HttpStatus.ACCEPTED);
  }

  @GetMapping("/one-many/{taskId}/result")
  public ResponseEntity<OneManyOutput> oneManyResult(@PathVariable String taskId) {
    return new ResponseEntity<>(oneManyService.oneManyResult(taskId), HttpStatus.ACCEPTED);
  }

  @GetMapping(value = "/one-many/secondary/structure/{modelId}")
  public ResponseEntity<String> oneManySecondaryStructureModel(@PathVariable String modelId) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("image/svg+xml"))
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + modelId + ".svg" + "\"")
        .body(
            new String(
                oneManyService.oneManySecondaryStructureModel(modelId), StandardCharsets.UTF_8));
  }

  @GetMapping(value = "/one-many/tertiary/structure/{modelId}")
  public ResponseEntity<String> oneManyTertiaryStructureModel(@PathVariable String modelId) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("chemical/x-mmcif"))
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + modelId + ".cif" + "\"")
        .body(
            new String(
                oneManyService.oneManyTertiaryStructureModel(modelId), StandardCharsets.UTF_8));
  }

  @GetMapping(value = "/one-many/example/{example}")
  public ResponseEntity<TaskIdResponse> oneManyExample(@PathVariable String example) {
    return new ResponseEntity<>(oneManyService.oneManyExample(example), HttpStatus.ACCEPTED);
  }
}
