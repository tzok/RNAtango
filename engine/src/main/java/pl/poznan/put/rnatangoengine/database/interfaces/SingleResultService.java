package pl.poznan.put.rnatangoengine.database.interfaces;

import java.sql.Date;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;

public interface SingleResultService {
  SingleResultEntity getById(Long id);

  SingleResultEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);

  void deleteByRemoveAfterBefore(Date date);
}
