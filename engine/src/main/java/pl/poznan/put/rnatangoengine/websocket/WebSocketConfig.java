package pl.poznan.put.rnatangoengine.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(new SingleStatusWebSocketHandler(), "/ws/single")
        .addHandler(new ManyManyStatusWebSocketHandler(), "/ws/manymany")
        .addHandler(new OneManyStatusWebSocketHandler(), "/ws/onemany")
        .withSockJS();
    // .addInterceptors(
    //     new HandshakeInterceptor() {
    //       @Override
    //       public boolean beforeHandshake(
    //           ServerHttpRequest request,
    //           ServerHttpResponse response,
    //           WebSocketHandler wsHandler,
    //           Map<String, Object> attributes)
    //           throws Exception {

    //         // Get the URI segment corresponding to the auction id during handshake
    //         String path = request.getURI().getPath();
    //         String hashId = path.substring(path.lastIndexOf('/') + 1);

    //         // This will be added to the websocket session
    //         attributes.put("hashId", hashId);
    //         return true;
    //       }

    //       @Override
    //       public void afterHandshake(
    //           ServerHttpRequest request,
    //           ServerHttpResponse response,
    //           WebSocketHandler wsHandler,
    //           Exception exception) {
    //         // Nothing to do after handshake
    //       }
    //     });
  }
}
