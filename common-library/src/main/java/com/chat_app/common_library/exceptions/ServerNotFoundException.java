package com.chat_app.common_library.exceptions;

public class ServerNotFoundException extends RuntimeException {
  public ServerNotFoundException(String message) {
    super(message);
  }
}
