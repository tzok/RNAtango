package pl.poznan.put.rnatangoengine.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableStatusResponse;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.StatusInput;

@Service
public class SingleStatusWebSocketHandler extends TextWebSocketHandler {
  @Autowired SingleResultRepository singleRepository;
  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(TextWebSocketHandler.class);
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

    sessions.forEach(
        webSocketSession -> {
          try {
            StatusInput incoming =
                new ObjectMapper().readValue(message.getPayload(), StatusInput.class);
            String taskHashId = incoming.hashId();
            try {
              SingleResultEntity _singleResultEntity =
                  singleRepository.getByHashId(UUID.fromString(taskHashId));
              if (_singleResultEntity.getStatus().equals(Status.FAILED)) {
                webSocketSession.sendMessage(
                    new TextMessage(
                        new Gson()
                            .toJson(
                                ImmutableStatusResponse.builder()
                                    .error(_singleResultEntity.getUserErrorLog())
                                    .build())));
              } else {
                webSocketSession.sendMessage(
                    new TextMessage(
                        new Gson()
                            .toJson(
                                ImmutableStatusResponse.builder()
                                    .status(_singleResultEntity.getStatus())
                                    .resultUrl("/single/" + taskHashId + "/result")
                                    .build())));
              }
            } catch (Exception e) {
              webSocketSession.sendMessage(
                  new TextMessage(
                      new Gson()
                          .toJson(
                              ImmutableStatusResponse.builder()
                                  .error("Task does not exist")
                                  .build())));
            }
          } catch (Exception e) {
            try {
              TextMessage returnMessage =
                  new TextMessage(
                      new Gson()
                          .toJson(
                              ImmutableStatusResponse.builder()
                                  .error("Message is not acceptable")
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
