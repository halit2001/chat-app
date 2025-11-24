package com.chatapp.websocket_service.handler;

import com.chatapp.websocket_service.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MyWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> channelSessions = new ConcurrentHashMap<>();
    private final Object channelLock = new Object();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");

        if (userId == null || username == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing authentication data"));
            return;
        }

        sessions.put(userId, session);
        logger.info("WebSocket connected: userId={}, username={}", userId, username);

        sendJsonMessage(session, Map.of(
                "type", "system_message",
                "content", "Welcome " + username + "! WebSocket connection established."
        ));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (!(message instanceof TextMessage textMessage)) return;

        String payload = textMessage.getPayload();
        ChatMessage msg;
        try {
            msg = mapper.readValue(payload, ChatMessage.class);
        } catch (Exception ex) {
            logger.error("Invalid JSON from session [{}]: {}", session.getAttributes().get("userId"), payload, ex);
            sendJsonMessage(session, Map.of("type", "error", "content", "Invalid JSON format"));
            return;
        }

        String action = msg.getAction();
        if (action == null || action.isBlank()) {
            sendJsonMessage(session, Map.of("type", "error", "content", "Missing required field: action"));
            return;
        }

        switch (action) {
            case "join" -> handleJoin(session, msg.getChannelId());
            case "send_message" -> handleSendMessage(session, msg.getChannelId(), msg.getContent());
            case "logout" -> handleLogout(session);
            default -> sendJsonMessage(session, Map.of("type", "error", "content", "Unknown action: " + action));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error [{}]: {}", session.getAttributes().get("userId"), exception.getMessage(), exception);
        if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        cleanupSession(session);
        logger.info("Session closed [{}]: {}", session.getAttributes().get("userId"), closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleJoin(WebSocketSession session, String channelId) {
        if (channelId == null || channelId.isBlank()) {
            sendJsonMessage(session, Map.of("type", "error", "content", "Missing channelId"));
            return;
        }
        String userId = (String) session.getAttributes().get("userId");
        synchronized (channelLock) {
            channelSessions.values().forEach(set -> set.remove(userId));
            Set<String> userSet = channelSessions.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet());
            userSet.add(userId);
        }
        sendJsonMessage(session, Map.of("type", "info", "content", "Joined channel: " + channelId));
    }

    private void handleSendMessage(WebSocketSession session, String channelId, String content) {
        if (channelId == null || channelId.isBlank()) {
            sendJsonMessage(session, Map.of("type", "error", "content", "Missing channelId"));
            return;
        }
        if (content == null || content.isBlank()) {
            sendJsonMessage(session, Map.of("type", "error", "content", "Missing content"));
            return;
        }

        String userId = (String) session.getAttributes().get("userId");
        Set<String> recipients = channelSessions.get(channelId);
        if (recipients == null || recipients.isEmpty()) return;

        synchronized (channelLock) {
            for (String recipientId : recipients) {
                WebSocketSession recipient = sessions.get(recipientId);
                if (recipient != null && recipient.isOpen()) {
                    sendJsonMessage(recipient, Map.of(
                            "type", "chat_message",
                            "channelId", channelId,
                            "from", userId,
                            "content", content
                    ));
                }
            }
        }
    }

    private void handleLogout(WebSocketSession session) {
        cleanupSession(session);
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                logger.warn("Failed to close session: {}", e.getMessage());
            }
        }
    }

    private void cleanupSession(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) return;
        synchronized (channelLock) {
            sessions.remove(userId);
            channelSessions.values().forEach(set -> set.remove(userId));
        }
    }

    private void sendJsonMessage(WebSocketSession session, Map<String, Object> payload) {
        if (session == null || !session.isOpen()) return;
        try {
            String jsonMessage = mapper.writeValueAsString(payload);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            logger.warn("Failed to send message to session [{}]: {}", session.getAttributes().get("userId"), e.getMessage());
        }
    }
}
