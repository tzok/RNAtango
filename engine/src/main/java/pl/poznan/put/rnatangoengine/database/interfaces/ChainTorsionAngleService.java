package pl.poznan.put.rnatangoengine.database.interfaces;

import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.ChainTorsionAngleEntity;

public interface ChainTorsionAngleService {
  ChainTorsionAngleEntity getById(Long id);

  ChainTorsionAngleEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
