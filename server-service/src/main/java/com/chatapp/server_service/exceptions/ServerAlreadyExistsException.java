package com.chatapp.server_service.exceptions;

public class ServerAlreadyExistsException extends RuntimeException {
    public ServerAlreadyExistsException(String message) {
        super(message);
    }
}
