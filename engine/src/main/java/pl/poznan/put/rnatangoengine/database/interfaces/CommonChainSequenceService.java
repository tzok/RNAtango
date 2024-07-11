package pl.poznan.put.rnatangoengine.database.interfaces;

import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.CommonChainSequenceEntity;

public interface CommonChainSequenceService {
  CommonChainSequenceEntity getById(Long id);

  CommonChainSequenceEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
