package pl.poznan.put.rnatangoengine.database.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.interfaces.FileService;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;

@Service
public class DefaultFileService implements FileService {
  @Autowired private final FileRepository repository;

  @Autowired
  public DefaultFileService(FileRepository repository) {
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
