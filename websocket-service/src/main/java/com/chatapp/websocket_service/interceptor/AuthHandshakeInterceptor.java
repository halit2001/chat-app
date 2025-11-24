package com.chatapp.websocket_service.interceptor;

import com.chatapp.websocket_service.client.AuthClient;
import com.chatapp.websocket_service.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandshakeInterceptor.class);

    @Autowired
    private AuthClient authClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        URI uri = request.getURI();
        if (uri == null) {
            logger.warn("Handshake failed: missing URI ");
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        String query = uri.getQuery();
        if (query == null || !query.contains("token=")) {
            logger.warn("Handshake failed: missing token");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String token = query.split("token=")[1];
        try {
            Map<String, Object> authResponse = authClient.validateToken(token);
            if (authResponse == null || !Boolean.TRUE.equals(authResponse.get("valid"))) {
                logger.warn("Handshake failed: invalid token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            if (userId == null || username == null) {
                logger.warn("Handshake failed: unknown user attempt");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("userId", userId);
            attributes.put("username", username);

            return true;
        } catch (Exception ex) {
            logger.error("Auth service call failed: {}", ex.getMessage(), ex);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        logger.info("Handshake established successfully.");
    }
}
