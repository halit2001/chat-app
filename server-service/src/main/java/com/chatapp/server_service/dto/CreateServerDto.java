package com.chatapp.server_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateServerDto {
    @NotBlank(message = "Server name cannot be blank.")
    private String serverName;

    @NotBlank(message = "Description cannot be blank.")
    private String description;
}
