package com.chatapp.server_service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ServerDetailsResponse {
    private String id;
    private String ownerId;
    private String serverName;
    private String description;
    private String iconUrl;
    private List<String> memberIds;
    private List<String> channelIds;
}
