package com.chatapp.websocket_service.config;

import com.chatapp.websocket_service.handler.MyWebSocketHandler;
import com.chatapp.websocket_service.interceptor.AuthHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final MyWebSocketHandler myWebSocketHandler;
    private final AuthHandshakeInterceptor authInterceptor;

    public WebSocketConfig(MyWebSocketHandler myWebSocketHandler, AuthHandshakeInterceptor authInterceptor) {
        this.myWebSocketHandler = myWebSocketHandler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myWebSocketHandler, "/ws")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*");
    }
}
