package pl.poznan.put.rnatangoengine.database.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.CommonChainSequenceEntity;

@Repository
public interface CommonChainSequenceRepository
    extends JpaRepository<CommonChainSequenceEntity, Long> {
  CommonChainSequenceEntity getById(Long id);

  CommonChainSequenceEntity getByHashId(UUID hashId);
}
