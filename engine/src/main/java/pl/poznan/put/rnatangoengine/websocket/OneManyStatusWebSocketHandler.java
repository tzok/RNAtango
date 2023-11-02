package pl.poznan.put.rnatangoengine.websocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class OneManyStatusWebSocketHandler extends TextWebSocketHandler {

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
    String hashId = (String) session.getAttributes().get("hashId");
    sessions.forEach(
        webSocketSession -> {
          try {
            webSocketSession.sendMessage(message);
          } catch (IOException e) {
            try {
              webSocketSession.close();
            } catch (IOException e1) {
            }
            // LOGGER.error("Error occurred.", e);
          }
        });
  }
}
