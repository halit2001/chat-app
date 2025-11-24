package com.chatapp.server_service.service;

import com.chat_app.common_library.response.ServerComprehensivePermissionsResponse;
import com.chatapp.server_service.dto.AddMemberRequest;
import com.chatapp.server_service.dto.CreateServerDto;
import com.chatapp.server_service.dto.JoinRequestDto;
import com.chatapp.server_service.dto.UpdateServerDto;
import com.chatapp.server_service.response.CreatedServerResponse;
import com.chat_app.common_library.response.MemberDetailsResponse;
import com.chatapp.server_service.response.ServerDetailsResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ServerService {
    ServerDetailsResponse getServerDetails(String userId, String serverId);

    CreatedServerResponse createNewServer(String userId, CreateServerDto createServerDto, MultipartFile file) throws IOException;

    List<ServerDetailsResponse> getUserServers(String userId);

    ServerDetailsResponse updateServerDetails(String userId, String serverId, UpdateServerDto updateServerDto, MultipartFile file);

    String addMemberToServer(String userId, String serverId, AddMemberRequest memberRequest);

    void removeMemberFromServer(String userId, String serverId, String memberId);

    List<MemberDetailsResponse> getMemberDetailsWithServer(String userId, String serverId);

    void joinServer(String userId, JoinRequestDto joinRequestDto);

    Boolean isOwnerOfServer(String userId, String serverId);

    Boolean hasServerAccess(String serverId, String userId);

    Boolean isUserMemberOfServer(String serverId, String memberId);

    ServerComprehensivePermissionsResponse getComprehensiveServerPermissions(String serverId, List<String> userIds);

    void addChannelToServer(String serverId, String channelId);

    Boolean removeChannelFromServer(String userId, String serverId, String channelId);
}
