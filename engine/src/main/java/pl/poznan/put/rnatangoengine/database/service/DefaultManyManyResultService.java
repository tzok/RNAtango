package pl.poznan.put.rnatangoengine.database.service;

import java.sql.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.interfaces.ManyManyService;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;

@Service
public class DefaultManyManyResultService implements ManyManyService {
  @Autowired private final ManyManyRepository repository;

  @Autowired
  public DefaultManyManyResultService(ManyManyRepository repository) {
    this.repository = repository;
  }

  @Override
  public ManyManyResultEntity getById(Long id) {
    return repository.getById(id);
  }

  @Override
  public ManyManyResultEntity getByHashId(UUID hashId) {
    return repository.getByHashId(hashId);
  }

  @Override
  public void deleteById(Long id) {
    repository.deleteById(id);
  }

  @Override
  public void deleteByHashId(UUID hashId) {
    repository.deleteByHashId(hashId);
  }

  @Override
  public void deleteByRemoveAfterBefore(Date date) {
    repository.deleteByRemoveAfterBefore(date);
  }
}
