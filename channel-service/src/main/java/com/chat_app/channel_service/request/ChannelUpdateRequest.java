package com.chat_app.channel_service.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChannelUpdateRequest {
    @Size(min = 3, max = 50, message = "Channel name must be between 1 and 50 characters")
    private String channelName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Size(max = 5, message = "Maximum 5 topics allowed")
    private List<String> topic;
}
