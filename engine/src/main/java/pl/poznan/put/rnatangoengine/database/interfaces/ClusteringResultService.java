package pl.poznan.put.rnatangoengine.database.interfaces;

import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.ClusteringResultEntity;

public interface ClusteringResultService {
  ClusteringResultEntity getById(Long id);

  ClusteringResultEntity getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
