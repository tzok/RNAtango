package pl.poznan.put.rnatangoengine.database.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.interfaces.SingleResultService;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;

@Service
public class DefaultSingleResultService implements SingleResultService {
  private final SingleResultRepository repository;

  @Autowired
  public DefaultSingleResultService(SingleResultRepository repository) {
    this.repository = repository;
  }

  @Override
  public SingleResultEntity getById(Long id) {
    return repository.getById(id);
  }

  @Override
  public SingleResultEntity getByHashId(UUID hashId) {
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
}
