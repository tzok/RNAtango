package pl.poznan.put.rnatangoengine.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.poznan.put.rnatangoengine.dto.StructureFileInput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;

@RestController
public class Upload {
  @PostMapping("/upload")
  public StructureFileOutput upload(@RequestBody StructureFileInput structureFileInput) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
