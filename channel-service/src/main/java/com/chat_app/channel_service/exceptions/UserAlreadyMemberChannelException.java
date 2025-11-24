package com.chat_app.channel_service.exceptions;

public class UserAlreadyMemberChannelException extends RuntimeException {
    public UserAlreadyMemberChannelException(String message) {
        super(message);
    }
}
