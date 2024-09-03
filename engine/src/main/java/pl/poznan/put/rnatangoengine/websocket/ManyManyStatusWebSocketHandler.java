package pl.poznan.put.rnatangoengine.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.StatusInput;

@Service
public class ManyManyStatusWebSocketHandler extends TextWebSocketHandler {
  @Autowired ManyManyRepository manyManyRepository;

  private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.add(session);
    super.afterConnectionEstablished(session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session);
    super.afterConnectionClosed(session, status);
  }

  private ManyManyResultEntity getTaskStatus(WebSocketSession session, String taskHashId)
      throws Exception {

    try {
      return manyManyRepository.getByHashId(UUID.fromString(taskHashId));
    } catch (Exception e) {
      session.sendMessage(
          new TextMessage(
              new Gson()
                  .toJson(ImmutableStatusResponse.builder().error("Task does not exist").build())));
      session.close(CloseStatus.NORMAL);
      return null;
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    super.handleTextMessage(session, message);
    StatusInput incoming;
    try {
      incoming = new ObjectMapper().readValue(message.getPayload(), StatusInput.class);
    } catch (Exception e) {
      TextMessage returnMessage =
          new TextMessage(
              new Gson()
                  .toJson(
                      ImmutableStatusResponse.builder()
                          .error("Message is not acceptable")
                          .build()));
      session.sendMessage(returnMessage);
      session.close(CloseStatus.NOT_ACCEPTABLE);
      return;
    }
    try {
      ManyManyResultEntity _manyManyResultEntity = getTaskStatus(session, incoming.hashId());

      switch (_manyManyResultEntity.getStatus()) {
        case SUCCESS:
          session.sendMessage(
              new TextMessage(
                  new Gson()
                      .toJson(
                          ImmutableStatusResponse.builder()
                              .status(_manyManyResultEntity.getStatus())
                              .resultUrl("/many-many/" + incoming.hashId() + "/result")
                              .build())));
          session.close(CloseStatus.NORMAL);
          break;

        case FAILED:
          session.sendMessage(
              new TextMessage(
                  new Gson()
                      .toJson(
                          ImmutableStatusResponse.builder()
                              .error(_manyManyResultEntity.getUserErrorLog())
                              .build())));
          session.close(CloseStatus.NORMAL);
          break;

        default:
          session.sendMessage(
              new TextMessage(
                  new Gson()
                      .toJson(
                          ImmutableStatusResponse.builder()
                              .status(_manyManyResultEntity.getStatus())
                              .progress(_manyManyResultEntity.getProcessingProgess())
                              .build())));
      }
    } catch (Exception e) {
      session.sendMessage(new TextMessage("Error during sending message"));
    }
  }
}
