package pl.poznan.put.rnatangoengine.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.ChainTorsionAngleEntity;

@Repository
public interface ChainTorsionAngleRepository extends JpaRepository<ChainTorsionAngleEntity, Long> {
  ChainTorsionAngleEntity getById(Long id);

  void deleteById(Long id);
}
