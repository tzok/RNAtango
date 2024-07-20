package pl.poznan.put.rnatangoengine.database.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.rnatangoengine.database.definitions.WebPushSubscription;

@Repository
public interface WebPushRepository extends JpaRepository<WebPushSubscription, Long> {
  WebPushSubscription getById(Long id);

  WebPushSubscription getByHashId(UUID hashId);

  void deleteById(Long id);

  void deleteByHashId(UUID hashId);
}
