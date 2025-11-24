package com.chat_app.channel_service.mapper;

import com.chat_app.channel_service.model.Channel;
import com.chat_app.channel_service.model.ChannelType;
import com.chat_app.channel_service.request.ChannelUpdateRequest;
import com.chat_app.channel_service.request.CreateChannelRequest;
import com.chat_app.channel_service.response.ChannelResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class ChannelMapper {
    public Channel createChannelModel(String userId, String serverId, CreateChannelRequest createChannelRequest) {
        return Channel.builder()
                .serverId(serverId)
                .channelName(createChannelRequest.getChannelName())
                .isPrivate(createChannelRequest.getIsPrivate())
                .type(ChannelType.convertStringToType(createChannelRequest.getChannelType()))
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .description(createChannelRequest.getDescription())
                .topic(createChannelRequest.getTopic())
                .build();
    }

    public ChannelResponse convertChannelToResponse(Channel channel) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .serverId(channel.getServerId())
                .channelName(channel.getChannelName())
                .membersIds(channel.getMembersIds())
                .isPrivate(channel.getIsPrivate())
                .type(channel.getType())
                .createdBy(channel.getCreatedBy())
                .createdAt(channel.getCreatedAt())
                .description(channel.getDescription())
                .topic(channel.getTopic())
                .build();
    }

    public void updateChannel(Channel channel, ChannelUpdateRequest channelUpdateRequest) {
        if (channelUpdateRequest.getChannelName() != null)
            channel.setChannelName(channelUpdateRequest.getChannelName());
        if (channelUpdateRequest.getDescription() != null)
            channel.setDescription(channelUpdateRequest.getDescription());
        if (channelUpdateRequest.getTopic() != null) {
            channel.setTopic(new ArrayList<>(channelUpdateRequest.getTopic()));
        }
    }

}
