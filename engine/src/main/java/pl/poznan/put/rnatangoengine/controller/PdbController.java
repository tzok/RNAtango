package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.StructurePdbInput;
import pl.poznan.put.rnatangoengine.service.PdbService;

@RestController
public class PdbController {
  private final PdbService pdbService;

  @Autowired
  public PdbController(PdbService pdbService) {
    this.pdbService = pdbService;
  }

  @PostMapping("/pdb")
  public ResponseEntity<StructureFileOutput> pdb(@RequestBody StructurePdbInput structurePdbInput) {
    return new ResponseEntity<>(pdbService.pdb(structurePdbInput), HttpStatus.ACCEPTED);
  }
}
