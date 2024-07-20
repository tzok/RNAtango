package pl.poznan.put.rnatangoengine.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.LCSEntity;

@Repository
public interface LCSRepository extends JpaRepository<LCSEntity, Long> {
  LCSEntity getById(Long id);

  void deleteById(Long id);
}
