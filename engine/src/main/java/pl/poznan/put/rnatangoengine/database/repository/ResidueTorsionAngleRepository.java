package pl.poznan.put.rnatangoengine.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.ResidueTorsionAngleEntity;

@Repository
public interface ResidueTorsionAngleRepository
    extends JpaRepository<ResidueTorsionAngleEntity, Long> {
  ResidueTorsionAngleEntity getById(Long id);

  void deleteById(Long id);
}
