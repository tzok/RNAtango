package pl.poznan.put.rnatangoengine.database.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.ClusteredModelEntity;

@Repository
public interface ClusteredModelRepository extends JpaRepository<ClusteredModelEntity, Long> {
  ClusteredModelEntity getById(Long id);

  ClusteredModelEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
