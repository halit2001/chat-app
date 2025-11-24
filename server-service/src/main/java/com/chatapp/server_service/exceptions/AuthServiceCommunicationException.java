package com.chatapp.server_service.exceptions;

import org.springframework.http.HttpStatus;

public class AuthServiceCommunicationException extends RuntimeException {
    private final HttpStatus httpStatus;

    public AuthServiceCommunicationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public AuthServiceCommunicationException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
