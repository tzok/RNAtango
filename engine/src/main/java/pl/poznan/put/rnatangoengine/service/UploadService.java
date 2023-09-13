package pl.poznan.put.rnatangoengine.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
import pl.poznan.put.rnatangoengine.dto.File;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.Model;
import pl.poznan.put.rnatangoengine.dto.StructureFileInput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.logic.Structure;

@Service
public class UploadService {
  @Autowired FileRepository fileRepository;

  public StructureFileOutput upload(StructureFileInput structureFileInput) {
    try {
      File file = structureFileInput.file();
      if (file.content().length() > 1024 * 1024 * 1024 * 50) { // over 50MB
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "could not parse structure");
      }

      Structure structure = new Structure(file.content(), file.filename());
      List<Model> models = structure.getModels();

      FileEntity _fileEntity = fileRepository.saveAndFlush(new FileEntity(file, models));

      return ImmutableStructureFileOutput.builder()
          .fileHashId(_fileEntity.getHashId().toString())
          .addAllModels(models)
          .build();
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "could not parse structure");
    }
  }

  public void remove(String fileId) {
    fileRepository.deleteByHashId(UUID.fromString(fileId));
  }
}
