package pl.poznan.put.rnatangoengine.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.logic.Structure;

@Service
public class UploadService {
  @Autowired FileRepository fileRepository;

  private String readFileAsString(String filePath) throws IOException {
    StringBuffer fileData = new StringBuffer();
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(buf)) != -1) {
      String readData = String.valueOf(buf, 0, numRead);
      fileData.append(readData);
    }
    reader.close();
    return fileData.toString();
  }

  public StructureFileOutput upload(MultipartFile structureFileInput) {
    try {
      File tempFile = File.createTempFile("structure_", ".tmp");
      File transferFile = new File(tempFile.getAbsolutePath());
      structureFileInput.transferTo(transferFile);

      String fileContent = readFileAsString(tempFile.getAbsolutePath());
      Structure structure = new Structure(fileContent, structureFileInput.getOriginalFilename());
      structure.process();
      FileEntity _fileEntity =
          fileRepository.saveAndFlush(
              new FileEntity(structureFileInput.getOriginalFilename(), fileContent.getBytes()));

      return ImmutableStructureFileOutput.builder()
          .fileHashId(_fileEntity.getHashId().toString())
          .addAllModels(structure.getModels())
          .build();
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "could not parse structure");
    }
  }

  public void remove(String fileId) {
    fileRepository.deleteByHashId(UUID.fromString(fileId));
  }
}
