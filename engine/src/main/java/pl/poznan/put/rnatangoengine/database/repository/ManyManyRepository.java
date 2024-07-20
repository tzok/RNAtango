package pl.poznan.put.rnatangoengine.database.repository;

import java.sql.Date;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;

@Repository
public interface ManyManyRepository extends JpaRepository<ManyManyResultEntity, Long> {
  ManyManyResultEntity getById(Long id);

  ManyManyResultEntity getByHashId(UUID hashId);

  void deleteByHashId(UUID hashId);

  void deleteByRemoveAfterBefore(Date date);
}
