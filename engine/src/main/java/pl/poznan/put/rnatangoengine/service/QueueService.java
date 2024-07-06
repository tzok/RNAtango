package pl.poznan.put.rnatangoengine.service;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.dto.ImmutableTask;
import pl.poznan.put.rnatangoengine.dto.Task;
import pl.poznan.put.rnatangoengine.dto.TaskType;

@Service
public class QueueService {
  @Autowired private AmqpTemplate rabbitTemplate;
  @Autowired private Queue queue;

  private static Logger logger = LogManager.getLogger(QueueService.class);

  public void sendSingle(UUID hashId) throws Exception {
    Task task = ImmutableTask.builder().taskHashId(hashId).type(TaskType.Single).build();
    rabbitTemplate.convertAndSend(queue.getName(), task);
    logger.info("Sending Message to the Queue : " + task);
  }

  public void sendOneMany(UUID hashId) throws Exception {
    Task task = ImmutableTask.builder().taskHashId(hashId).type(TaskType.OneMany).build();
    rabbitTemplate.convertAndSend(queue.getName(), task);
    logger.info("Sending Message to the Queue : " + task);
  }
}
