package pl.poznan.put.rnatangoengine.database.interfaces;

import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.ResidueTorsionAngleEntity;

public interface ResidueTorsionAngleService {
  ResidueTorsionAngleEntity getById(Long id);

  ResidueTorsionAngleEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
