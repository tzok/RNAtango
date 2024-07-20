package pl.poznan.put.rnatangoengine.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ClusteringResult;
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
  public ResponseEntity<TaskIdResponse> manyManySubmitForm(
      @RequestBody ManyManySubmitFormInput input) {
    return new ResponseEntity<>(manyManyService.manyMany(input), HttpStatus.ACCEPTED);
  }

  @GetMapping("/many-many/{taskId}")
  public StatusResponse manyManyStatus(@PathVariable String taskId) {
    return manyManyService.manyManyStatus(taskId);
  }

  @GetMapping("/many-many/{taskId}/result")
  public ManyManyOutput manyManyResult(@PathVariable String taskId) {
    return manyManyService.manyManyResult(taskId);
  }

  @GetMapping("/many-many/{taskId}/clustering")
  public List<ClusteringResult> manyManyClusteringResult(@PathVariable String taskId) {
    return manyManyService.manyManyClusteringResult(taskId);
  }

  @GetMapping("/many-many/{taskId}/dendrogram")
  public ResponseEntity<String> manyManyDendrogram(@PathVariable String taskId) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("image/svg+xml"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + taskId + ".svg" + "\"")
        .body(new String(manyManyService.manyManyDendrogram(taskId), StandardCharsets.UTF_8));
  }

  @GetMapping(value = "/many-many/example/{example}")
  public ResponseEntity<TaskIdResponse> oneManyExample(@PathVariable String example) {
    return new ResponseEntity<>(manyManyService.manyManyExample(example), HttpStatus.ACCEPTED);
  }
}
