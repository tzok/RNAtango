package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.StructureFileInput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.service.UploadService;

@RestController
public class UploadController {
  private final UploadService uploadService;

  @Autowired
  public UploadController(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  @PostMapping("/upload")
  public ResponseEntity<TaskIdResponse> upload(@RequestBody StructureFileInput structureFileInput) {
    return new ResponseEntity<>(uploadService.upload(structureFileInput), HttpStatus.ACCEPTED);
  }

  @GetMapping("/upload/{taskId}")
  public StatusResponse uploadStatus(@PathVariable String taskId) {
    return uploadService.uploadStatus(taskId);
  }

  @GetMapping("/upload/{taskId}/result")
  public StructureFileOutput uploadResult(@PathVariable String taskId) {
    return uploadService.uploadResult(taskId);
  }
}
