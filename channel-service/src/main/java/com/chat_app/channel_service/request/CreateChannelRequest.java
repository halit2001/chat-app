package com.chat_app.channel_service.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelRequest {
    @NotBlank(message = "Channel name cannot be blank")
    @Size(min = 3, max = 50, message = "Channel name must be between 1 and 50 characters")
    private String channelName;

    @NotNull(message = "Channel privacy must be specified")
    private Boolean isPrivate;

    @NotBlank(message = "Channel type is required")
    private String channelType;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Size(max = 5, message = "Maximum 5 topics allowed")
    private List<String> topic;
}
