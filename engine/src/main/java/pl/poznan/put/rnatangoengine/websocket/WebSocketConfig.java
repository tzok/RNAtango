package pl.poznan.put.rnatangoengine.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  @Autowired SingleStatusWebSocketHandler singleStatusWebSocketHandler;
  @Autowired OneManyStatusWebSocketHandler oneManyStatusWebSocketHandler;
  @Autowired ManyManyStatusWebSocketHandler manyManyStatusWebSocketHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(singleStatusWebSocketHandler, "/ws/single")
        .addHandler(manyManyStatusWebSocketHandler, "/ws/manymany")
        .addHandler(oneManyStatusWebSocketHandler, "/ws/onemany")
        .setAllowedOrigins("*")
    // .withSockJS();
    // .addInterceptors(
    //     new HandshakeInterceptor() {

    //       @Override
    //       public boolean beforeHandshake(
    //           ServerHttpRequest request,
    //           ServerHttpResponse response,
    //           WebSocketHandler wsHandler,
    //           Map<String, Object> attributes)
    //           throws Exception {
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
    //           Exception exception) {}
    //     })
    ;
  }
}
