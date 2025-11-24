package com.chatapp.server_service.config;

import com.chatapp.server_service.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.chat_app.common_library.exceptions.ServerNotFoundException;
import com.chat_app.common_library.exceptions.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleServerNotFound(ServerNotFoundException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServerAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleServerAlreadyExists(ServerAlreadyExistsException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleServerAlreadyExists(AccessDeniedException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleInternalServerError(Exception ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MediaServiceCommunicationException.class)
    public ResponseEntity<Map<String, String>> handleMediaServiceCommunicationError(MediaServiceCommunicationException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MemberAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleMemberAlreadyExistsException(MemberAlreadyExistsException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthServiceCommunicationException.class)
    public ResponseEntity<Map<String, String>> handleAuthServiceCommunicationException(AuthServiceCommunicationException ex) {
        HttpStatus httpStatus = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), ex.getHttpStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }


}
