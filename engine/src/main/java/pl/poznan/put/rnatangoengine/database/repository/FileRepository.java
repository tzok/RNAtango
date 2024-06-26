package pl.poznan.put.rnatangoengine.database.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
  @Transactional
  @Query("select f FROM FileEntity f WHERE f.id = ?1")
  FileEntity getById(Long id);

  @Transactional
  @Query("select f FROM FileEntity f WHERE f.hashId = ?1")
  FileEntity getByHashId(UUID hashId);

  @Transactional
  @Modifying
  @Query("delete from FileEntity f where f.id = ?1")
  void deleteById(Long id);

  @Transactional
  @Modifying
  @Query("delete from FileEntity f where f.hashId = ?1")
  void deleteByHashId(UUID hashId);
}
