package com.chatapp.server_service.exceptions;

public class MediaServiceCommunicationException extends RuntimeException {
    public MediaServiceCommunicationException(String message) {
        super(message);
    }
    public MediaServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
