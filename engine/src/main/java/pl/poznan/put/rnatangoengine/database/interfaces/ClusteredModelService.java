package pl.poznan.put.rnatangoengine.database.interfaces;

import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.ClusteredModelEntity;

public interface ClusteredModelService {
  ClusteredModelEntity getById(Long id);

  ClusteredModelEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
