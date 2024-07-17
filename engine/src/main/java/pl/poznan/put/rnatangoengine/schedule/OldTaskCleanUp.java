package pl.poznan.put.rnatangoengine.schedule;

import java.sql.Date;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;

@Component
public class OldTaskCleanUp {
  @Autowired SingleResultRepository singleRepository;
  @Autowired OneManyRepository oneManyRepository;
  @Autowired ManyManyRepository manyManyRepository;

  @Scheduled(cron = "0 0 0 * * ?")
  public void doCleanUpEveryDay() {
    singleRepository.deleteByRemoveAfterBefore(Date.valueOf(LocalDate.now()));
    oneManyRepository.deleteByRemoveAfterBefore(Date.valueOf(LocalDate.now()));
    manyManyRepository.deleteByRemoveAfterBefore(Date.valueOf(LocalDate.now()));
  }
}
