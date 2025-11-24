package com.chatapp.server_service.mapper;

import com.chat_app.common_library.response.ServerComprehensivePermissionsResponse;
import com.chat_app.common_library.response.UserServerPermissions;
import com.chatapp.server_service.dto.CreateServerDto;
import com.chatapp.server_service.model.Server;
import com.chatapp.server_service.response.CreatedServerResponse;
import com.chatapp.server_service.response.ServerDetailsResponse;
import com.chatapp.server_service.util.ServerPermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServerMapper {
    @Autowired
    private ServerPermissionUtil serverPermissionUtil;

    public Server createServer(String userId, CreateServerDto createServerDto, String iconUrl, String cloudinaryPublicId) {
        return Server
                .builder()
                .serverName(createServerDto.getServerName())
                .description(createServerDto.getDescription())
                .createdAt(LocalDateTime.now())
                .ownerId(userId)
                .iconUrl(iconUrl)
                .cloudinaryPublicId(cloudinaryPublicId)
                .build();
    }

    public CreatedServerResponse convertServerModelToCreatedServerResponse(Server server) {
        return CreatedServerResponse.builder()
                .id(server.getId())
                .serverName(server.getServerName())
                .description(server.getDescription())
                .ownerId(server.getOwnerId())
                .createdAt(server.getCreatedAt())
                .iconUrl(server.getIconUrl())
                .build();
    }

    public ServerDetailsResponse convertServerModelToServerDetailsResponse(Server server) {
        return ServerDetailsResponse.builder()
                .id(server.getId())
                .serverName(server.getServerName())
                .ownerId(server.getOwnerId())
                .description(server.getDescription())
                .iconUrl(server.getIconUrl())
                .memberIds(server.getMembersIds())
                .channelIds(server.getChannelIds())
                .build();
    }

    public ServerComprehensivePermissionsResponse createServerComprehensivePermissionsResponse(Server server, List<String> userIds) {
        Map<String, UserServerPermissions> permissionsMap = new HashMap<>();
        for (String userId : userIds) {
            UserServerPermissions userServerPermissions = UserServerPermissions
                    .builder()
                    .isOwner(serverPermissionUtil.isOwner(server, userId))
                    .isMember(serverPermissionUtil.isMember(server, userId))
                    .build();
            permissionsMap.put(userId, userServerPermissions);
        }
        return ServerComprehensivePermissionsResponse.builder()
                .serverId(server.getId())
                .serverName(server.getServerName())
                .description(server.getDescription())
                .membersIds(server.getMembersIds())
                .channelIds(server.getChannelIds())
                .createdAt(server.getCreatedAt())
                .iconUrl(server.getIconUrl())
                .userPermissions(permissionsMap)
                .build();
    }

}
