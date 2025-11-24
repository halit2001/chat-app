package com.chatapp.server_service.exceptions;

public class MemberAlreadyExistsException extends RuntimeException {
    public MemberAlreadyExistsException(String message) {
        super(message);
    }
}
