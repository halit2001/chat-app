package com.chatapp.websocket_service.model;

import jakarta.validation.constraints.NotBlank;

public class ChatMessage {
    @NotBlank(message = "Action cannot be empty")
    private String action;
    private String channelId;
    private String content;

    public ChatMessage() {
    }

    public ChatMessage(String action, String channelId, String content) {
        this.action = action;
        this.channelId = channelId;
        this.content = content;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}