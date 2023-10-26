package pl.poznan.put.rnatangoengine.database.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;

@Repository
public interface SingleResultRepository extends JpaRepository<SingleResultEntity, Long> {
  SingleResultEntity getById(Long id);

  SingleResultEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
