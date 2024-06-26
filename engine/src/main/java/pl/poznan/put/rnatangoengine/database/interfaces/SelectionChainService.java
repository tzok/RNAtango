package pl.poznan.put.rnatangoengine.database.interfaces;

import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;

public interface SelectionChainService {
  SelectionChainEntity getById(Long id);

  SelectionChainEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
