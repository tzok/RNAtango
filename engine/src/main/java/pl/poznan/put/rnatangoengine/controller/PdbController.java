package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
  public StructureFileOutput pdb(@RequestBody StructurePdbInput structurePdbInput) {
    return pdbService.pdb(structurePdbInput);
  }
}
