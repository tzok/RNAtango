package pl.poznan.put.rnatangoengine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.StructurePdbInput;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;

@Service
public class PdbService {
  @Autowired StructureProcessingService structureProcessingService;

  public StructureFileOutput pdb(StructurePdbInput structurePdbInput) {

    if (structurePdbInput.name().length() == 4) {
      try {
        Structure structure = structureProcessingService.process(structurePdbInput.name());
        return ImmutableStructureFileOutput.builder()
            .fileHashId(structurePdbInput.name())
            .addAllModels(structure.getModels())
            .build();
      } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "structure parsing error");
      }
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "structure does not exist");
    }
  }
}
