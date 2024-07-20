package pl.poznan.put.rnatangoengine.database.interfaces;

import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;

public interface LCSService {
  StructureModelEntity getById(Long id);

  void deleteById(Long id);
}
