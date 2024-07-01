package pl.poznan.put.rnatangoengine.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.StatusInput;

@Service
public class SingleStatusWebSocketHandler extends TextWebSocketHandler {
  @Autowired SingleResultRepository singleRepository;
  private static final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketHandler.class);
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

  private SingleResultEntity getTaskStatus(WebSocketSession session, String taskHashId)
      throws Exception {

    try {
      return singleRepository.getByHashId(UUID.fromString(taskHashId));
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
      SingleResultEntity _singleResultEntity = getTaskStatus(session, incoming.hashId());

      switch (_singleResultEntity.getStatus()) {
        case SUCCESS:
          session.sendMessage(
              new TextMessage(
                  new Gson()
                      .toJson(
                          ImmutableStatusResponse.builder()
                              .status(_singleResultEntity.getStatus())
                              .resultUrl("/single/" + incoming.hashId() + "/result")
                              .build())));
          session.close(CloseStatus.NORMAL);
          break;

        case FAILED:
          session.sendMessage(
              new TextMessage(
                  new Gson()
                      .toJson(
                          ImmutableStatusResponse.builder()
                              .error(_singleResultEntity.getUserErrorLog())
                              .build())));
          session.close(CloseStatus.NORMAL);
          break;

        default:
          session.sendMessage(
              new TextMessage(
                  new Gson()
                      .toJson(
                          ImmutableStatusResponse.builder()
                              .status(_singleResultEntity.getStatus())
                              .build())));
      }
    } catch (Exception e) {
      session.sendMessage(new TextMessage("Error during sending message"));
      LOGGER.error(e.getStackTrace().toString());
    }
  }
}