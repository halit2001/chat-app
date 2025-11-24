package com.chat_app.common_library.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ServerComprehensivePermissionsResponse {
    private String serverId;
    private String serverName;
    private String description;
    private List<String> membersIds;
    private List<String> channelIds;
    private String iconUrl;
    private LocalDateTime createdAt;
    private Map<String, UserServerPermissions> userPermissions;
}