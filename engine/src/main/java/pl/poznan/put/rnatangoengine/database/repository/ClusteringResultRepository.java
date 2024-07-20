package pl.poznan.put.rnatangoengine.database.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.ClusteringResultEntity;

@Repository
public interface ClusteringResultRepository extends JpaRepository<ClusteringResultEntity, Long> {
  ClusteringResultEntity getById(Long id);

  ClusteringResultEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
