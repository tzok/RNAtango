package pl.poznan.put.rnatangoengine.websocket;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusRequestError;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;

public class SingleStatusWebSocketHandler extends TextWebSocketHandler {
  @Autowired SingleResultRepository singleRepository;
  //   private static final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketHandler.class);
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

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    super.handleTextMessage(session, message);
    // String hashId = (String) session.getAttributes().get("hashId");

    sessions.forEach(
        webSocketSession -> {
          try {
            HashMap<String, String> incoming =
                new Gson().fromJson(message.getPayload(), HashMap.class); // TODO: map to immutable
            String taskHashId = incoming.get("hashId");
            try {
              SingleResultEntity _singleResultEntity =
                  singleRepository.getByHashId(UUID.fromString(taskHashId));
              TextMessage returnMessage =
                  new TextMessage(
                      new Gson()
                          .toJson(
                              ImmutableStatusResponse.builder()
                                  .status(_singleResultEntity.getStatus())
                                  .build()));
              webSocketSession.sendMessage(returnMessage);
            } catch (Exception e) {
              TextMessage returnMessage =
                  new TextMessage(
                      new Gson()
                          .toJson(
                              ImmutableStatusRequestError.builder()
                                  .reason("Task does not exist")
                                  .build()));
              webSocketSession.sendMessage(returnMessage);
            }
          } catch (Exception e) {
            try {
              TextMessage returnMessage =
                  new TextMessage(
                      new Gson()
                          .toJson(
                              ImmutableStatusRequestError.builder()
                                  .reason("Message is not acceptable")
                                  .build()));
              webSocketSession.sendMessage(returnMessage);
              webSocketSession.close(CloseStatus.NOT_ACCEPTABLE);
            } catch (IOException e1) {
            }
            // LOGGER.error("Error occurred.", e);
          }
        });
  }
}
