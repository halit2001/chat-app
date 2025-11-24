package com.chatapp.server_service.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CreatedServerResponse {
    private String id;
    private String serverName;
    private String ownerId;
    private String description;
    private String iconUrl;
    private LocalDateTime createdAt;
}
