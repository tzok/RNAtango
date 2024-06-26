package pl.poznan.put.rnatangoengine.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;

@Repository
public interface SelectionChainRepository extends JpaRepository<SelectionChainEntity, Long> {
  SelectionChainEntity getById(Long id);

  void deleteById(Long id);
}
