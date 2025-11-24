package com.chat_app.channel_service.response;

import com.chat_app.channel_service.model.ChannelType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ChannelResponse {
    private String id;
    private String serverId;
    private String channelName;
    private List<String> membersIds;
    private Boolean isPrivate;
    private ChannelType type;
    private String createdBy;
    private LocalDateTime createdAt;
    private String description;
    private List<String> topic;
}
