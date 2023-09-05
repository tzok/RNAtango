package pl.poznan.put.rnatangoengine.database.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.interfaces.IFileService;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;

@Service
public class FileService implements IFileService {
  private final FileRepository repository;

  @Autowired
  public FileService(FileRepository repository) {
    this.repository = repository;
  }

  @Override
  public FileEntity getById(Long id) {
    return repository.getById(id);
  }

  @Override
  public FileEntity getByHashId(UUID hashId) {
    return repository.getByHashId(hashId);
  }

  @Override
  public void deleteById(Long id) {
    repository.deleteById(id);
  }

  @Override
  public void deleteByHashId(UUID hashId) {
    repository.deleteByHashId(hashId);
  }
}
