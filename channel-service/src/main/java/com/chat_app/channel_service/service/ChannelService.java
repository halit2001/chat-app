package com.chat_app.channel_service.service;

import com.chat_app.channel_service.request.ChannelUpdateRequest;
import com.chat_app.channel_service.request.CreateChannelRequest;
import com.chat_app.channel_service.response.ChannelResponse;
import com.chat_app.common_library.response.MemberDetailsResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface ChannelService {
    ChannelResponse createChannel(String userId, String serverId, @Valid CreateChannelRequest createChannelRequest);

    ChannelResponse getChannelInformations(String userId, String channelId);

    ChannelResponse addMemberToChannel(String userId, String channelId, String memberId);

    List<ChannelResponse> getChannelsByServerId(String userId, String serverId);

    ChannelResponse updateChannel(String userId, String channelId, @Valid ChannelUpdateRequest channelUpdateRequest);

    void deleteChannel(String userId, String channelId);

    List<MemberDetailsResponse> getChannelMembers(String userId, String channelId);
}
