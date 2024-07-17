package pl.poznan.put.rnatangoengine.database.service;

import java.sql.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.interfaces.OneManyService;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;

@Service
public class DefaultOneManyResultService implements OneManyService {
  @Autowired private final OneManyRepository repository;

  @Autowired
  public DefaultOneManyResultService(OneManyRepository repository) {
    this.repository = repository;
  }

  @Override
  public OneManyResultEntity getById(Long id) {
    return repository.getById(id);
  }

  @Override
  public OneManyResultEntity getByHashId(UUID hashId) {
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
