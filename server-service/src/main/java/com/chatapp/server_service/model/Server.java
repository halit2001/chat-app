package com.chatapp.server_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "servers")
@Builder
public class Server {
    @Id
    private String id;

    @Field(targetType = FieldType.STRING)
    @Indexed(unique = true)
    private String serverName;

    private String ownerId;
    private String description;
    private List<String> membersIds = new ArrayList<>();
    private List<String> channelIds = new ArrayList<>();
    private String iconUrl;
    @Field(name = "cloudinary_public_id")
    private String cloudinaryPublicId;
    private LocalDateTime createdAt;
}