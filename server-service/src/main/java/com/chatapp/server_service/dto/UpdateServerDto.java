package com.chatapp.server_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateServerDto {
    private String serverName;
    private String description;
}
