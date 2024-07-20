package pl.poznan.put.rnatangoengine.database.repository;

import java.sql.Date;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;

@Repository
public interface OneManyRepository extends JpaRepository<OneManyResultEntity, Long> {
  OneManyResultEntity getById(Long id);

  OneManyResultEntity getByHashId(UUID hashId);

  void deleteByHashId(UUID hashId);

  void deleteByRemoveAfterBefore(Date date);
}
