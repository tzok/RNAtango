package pl.poznan.put.rnatangoengine.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.poznan.put.rnatangoengine.dto.Task;
import pl.poznan.put.rnatangoengine.logic.singleProcessing.SingleProcessing;

@Component
@RabbitListener(queues = "rabbitmq.queue", id = "listener")
public class QueueWorkerService {
  @Autowired SingleProcessing singleProcessing;

  private static Logger logger = LogManager.getLogger(QueueWorkerService.class.toString());

  @RabbitHandler
  public void receiver(Task task) {
    switch (task.type()) {
      case Single:
        singleProcessing.startTask(task.taskHashId());
        break;
      default:
        return;
    }
    logger.info(
        "Task listener invoked - Consuming Message with Task Identifier : " + task.toString());
  }
}
