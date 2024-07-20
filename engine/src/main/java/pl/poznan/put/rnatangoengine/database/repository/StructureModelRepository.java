package pl.poznan.put.rnatangoengine.database.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;

@Repository
public interface StructureModelRepository extends JpaRepository<StructureModelEntity, Long> {
  StructureModelEntity getById(Long id);

  StructureModelEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
