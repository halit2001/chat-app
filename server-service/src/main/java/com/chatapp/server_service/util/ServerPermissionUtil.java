package com.chatapp.server_service.util;

import com.chatapp.server_service.model.Server;
import com.chatapp.server_service.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerPermissionUtil {
    private final ServerRepository serverRepository;

    @Autowired
    public ServerPermissionUtil(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    public boolean isOwner(Server server, String userId) {
        return server.getOwnerId().equals(userId);
    }

    public boolean isMember(Server server, String userId) {
        return server.getMembersIds().contains(userId);
    }

    public boolean canManageServer(Server server, String userId) {
        return isOwner(server, userId);
    }

    public boolean canSeeServer(Server server, String userId) { return  isOwner(server, userId) || isMember(server, userId); }

}
