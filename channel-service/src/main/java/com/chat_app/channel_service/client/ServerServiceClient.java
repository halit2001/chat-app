package com.chat_app.channel_service.client;

import com.chat_app.channel_service.config.FeignConfig;
import com.chat_app.common_library.response.ServerComprehensivePermissionsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "server-service", path = "/api/server", configuration = FeignConfig.class)
public interface ServerServiceClient {
    @GetMapping("/{serverId}/is-owner")
    Boolean isOwnerOfServer(@RequestParam("userId") String userId, @PathVariable("serverId") String serverId);

    @GetMapping("/{serverId}/has-access/{userId}")
    Boolean hasServerAccess(@PathVariable("serverId") String serverId, @PathVariable("userId") String userId);

    @PostMapping("/{serverId}/comprehensive-permissions")
    ServerComprehensivePermissionsResponse getComprehensiveServerPermissions(@PathVariable("serverId") String serverId, @RequestBody List<String> userIds);

    @PostMapping("/{serverId}/channels/{channelId}")
    void addChannelToServer(@PathVariable("serverId") String serverId, @PathVariable("channelId") String channelId);

    @DeleteMapping("/{serverId}/channel/{channelId}/user/{userId}")
    Boolean removeChannelFromServer(@PathVariable("userId") String userId,
                                    @PathVariable("serverId") String serverId,
                                    @PathVariable("channelId") String channelId);
}
