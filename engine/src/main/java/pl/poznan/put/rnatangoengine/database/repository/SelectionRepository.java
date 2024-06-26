package pl.poznan.put.rnatangoengine.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;

@Repository
public interface SelectionRepository extends JpaRepository<SelectionEntity, Long> {
  SelectionEntity getById(Long id);

  void deleteById(Long id);
}
