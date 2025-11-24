package com.chat_app.channel_service.exceptions;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException(String message) {
        super(message);
    }
}
