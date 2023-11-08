package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.service.UploadService;

@RestController
public class UploadController {
  private final UploadService uploadService;

  @Autowired
  public UploadController(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  @PostMapping(
      path = "/upload",
      consumes = {"multipart/form-data"})
  public ResponseEntity<StructureFileOutput> upload(@RequestParam("file") MultipartFile file) {
    return new ResponseEntity<>(uploadService.upload(file), HttpStatus.OK);
  }

  @PostMapping("/upload/remove/{fileId}")
  public ResponseEntity<String> remove(@PathVariable String fileId) {
    uploadService.remove(fileId);
    return new ResponseEntity<>("", HttpStatus.OK);
  }
}
