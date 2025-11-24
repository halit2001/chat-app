package com.chat_app.channel_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "channels")
@Builder
public class Channel {
    @Id
    private String id;
    private String serverId;
    private String channelName;
    private List<String> membersIds = new ArrayList<>();
    private Boolean isPrivate;
    private ChannelType type;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private List<String> topic;
}
