package pl.poznan.put.rnatangoengine.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import pl.poznan.put.rnatangoengine.dto.Task;
import pl.poznan.put.rnatangoengine.dto.TaskType;

public class QueueService {

  public void send(String hashId, TaskType taskType) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("rabbit");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.queueDeclare("tasks", false, false, false, null);
    channel.basicPublish(
        "", "tasks", false, false, null, new Task(hashId, taskType).toString().getBytes());
  }
}
