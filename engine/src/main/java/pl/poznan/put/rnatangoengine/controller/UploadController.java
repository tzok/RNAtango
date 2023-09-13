package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.rnatangoengine.dto.StructureFileInput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.service.UploadService;

@RestController
public class UploadController {
  private final UploadService uploadService;

  @Autowired
  public UploadController(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  @PostMapping("/upload")
  public StructureFileOutput upload(@RequestBody StructureFileInput structureFileInput) {
    return uploadService.upload(structureFileInput);
  }

  @PostMapping("/upload/remove/{fileId}")
  public ResponseEntity<String> remove(@PathVariable String fileId) {
    uploadService.remove(fileId);
    return new ResponseEntity<>("", HttpStatus.OK);
  }
}
