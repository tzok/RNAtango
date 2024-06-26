package pl.poznan.put.rnatangoengine.database.interfaces;

import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;

public interface FileService {
  FileEntity getById(Long id);

  FileEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
