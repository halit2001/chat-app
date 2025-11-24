package com.chatapp.auth_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotNull
    @Size(min= 3, max= 20, message = "Username must be 3 and 20 length")
    private String username;

    @NotNull
    @Size(min = 4, max = 18, message = "Password must be between 4 and 18 length")
    private String password;
}
