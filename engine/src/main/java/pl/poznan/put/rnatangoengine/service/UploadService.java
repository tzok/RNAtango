package pl.poznan.put.rnatangoengine.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    File file = structureFileInput.file();
    Structure structure = new Structure(file.content());

    List<Model> models = structure.getModels();

    FileEntity _fileEntity = fileRepository.saveAndFlush(new FileEntity(file, models));

    return ImmutableStructureFileOutput.builder()
        .fileHashId(_fileEntity.getHashId().toString())
        .addAllModels(models)
        .build();
  }
}
