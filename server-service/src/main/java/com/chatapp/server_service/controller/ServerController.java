package com.chatapp.server_service.controller;

import com.chat_app.common_library.response.ServerComprehensivePermissionsResponse;
import com.chatapp.server_service.dto.AddMemberRequest;
import com.chatapp.server_service.dto.CreateServerDto;
import com.chatapp.server_service.dto.JoinRequestDto;
import com.chatapp.server_service.dto.UpdateServerDto;
import com.chatapp.server_service.response.CreatedServerResponse;
import com.chatapp.server_service.response.ServerDetailsResponse;
import com.chat_app.common_library.response.MemberDetailsResponse;
import com.chatapp.server_service.service.ServerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/server")
public class ServerController {
    @Autowired
    private ServerService serverService;

    @GetMapping(path = "/{serverId}/is-owner")
    public ResponseEntity<Boolean> isOwnerOfServer(@RequestParam String userId, @PathVariable("serverId") String serverId) {
        Boolean isOwner = serverService.isOwnerOfServer(userId, serverId);
        return ResponseEntity.status(HttpStatus.OK).body(isOwner);
    }

    @GetMapping(path = "/{serverId}/member/{memberId}")
    public ResponseEntity<Boolean> isUserMemberOfServer(@PathVariable("serverId") String serverId,
                                                        @PathVariable("memberId") String memberId) {
        Boolean isMember = serverService.isUserMemberOfServer(serverId, memberId);
        return ResponseEntity.status(HttpStatus.OK).body(isMember);
    }

    @GetMapping(path = "/{serverId}/has-access/{userId}")
    public ResponseEntity<Boolean> hasServerAccess(@PathVariable("serverId") String serverId,
                                                   @PathVariable("userId") String userId) {
        Boolean response = serverService.hasServerAccess(serverId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = "/{serverId}/details")
    public ResponseEntity<ServerDetailsResponse> getServerDetails(@RequestHeader("X-User-Id") String userId, @PathVariable("serverId") String serverId) {
        ServerDetailsResponse response = serverService.getServerDetails(userId, serverId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = "/user/get-all-servers")
    public ResponseEntity<Map<String, List<ServerDetailsResponse>>> getUserServers(@RequestHeader("X-User-Id") String userId) {
        List<ServerDetailsResponse> responses = serverService.getUserServers(userId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("servers", responses));
    }

    @GetMapping(path = "/{serverId}/members")
    public ResponseEntity<Map<String, List<MemberDetailsResponse>>> getServerMembers(@RequestHeader("X-User-Id") String userId,
                                                                                     @PathVariable("serverId") String serverId) {
        List<MemberDetailsResponse> memberDetailsResponses = serverService.getMemberDetailsWithServer(userId, serverId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("memberDetailsResponse", memberDetailsResponses));
    }

    @PostMapping("/{serverId}/comprehensive-permissions")
    public ResponseEntity<ServerComprehensivePermissionsResponse> getComprehensiveServerPermissions(
            @PathVariable("serverId") String serverId,
            @RequestBody List<String> userIds) {
        ServerComprehensivePermissionsResponse response = serverService.getComprehensiveServerPermissions(serverId, userIds);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/create-new-server")
    public ResponseEntity<Map<String, CreatedServerResponse>> createNewServer(@RequestHeader("X-User-Id") String userId,
                                                                              @Valid @RequestPart("createServerDto") CreateServerDto createServerDto,
                                                                              @RequestPart(value = "icon", required = false) MultipartFile file) throws IOException {
        CreatedServerResponse createdServerResponse = serverService.createNewServer(userId, createServerDto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("server", createdServerResponse));
    }

    @PostMapping("/{serverId}/channels/{channelId}")
    public ResponseEntity<Void> addChannelToServer(@PathVariable("serverId") String serverId, @PathVariable("channelId") String channelId) {
        serverService.addChannelToServer(serverId, channelId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/join-server")
    public ResponseEntity<Map<String, String>> joinServer(@RequestHeader("X-User-Id") String userId,
                                                          @RequestBody JoinRequestDto joinRequestDto) {
        serverService.joinServer(userId, joinRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Joined successfully"));
    }

    @PostMapping(path = "/{serverId}/members")
    public ResponseEntity<Map<String, String>> addMemberToServer(@RequestHeader("X-User-Id") String userId,
                                                                 @PathVariable("serverId") String serverId,
                                                                 @RequestBody AddMemberRequest memberRequest) {
        String memberIdToAdd = serverService.addMemberToServer(userId, serverId, memberRequest);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Member added successfully with memberId: " + memberIdToAdd));
    }

    @PutMapping(path = "/{serverId}/update-server-details")
    public ResponseEntity<Map<String, ServerDetailsResponse>> updateServerDetails(@RequestHeader("X-User-Id") String userId,
                                                                                  @PathVariable("serverId") String serverId,
                                                                                  @RequestPart("updateServerDto") UpdateServerDto updateServerDto,
                                                                                  @RequestPart(value = "icon", required = false) MultipartFile file) {
        ServerDetailsResponse response = serverService.updateServerDetails(userId, serverId, updateServerDto, file);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("updatedServer", response));
    }

    @DeleteMapping(path = "/{serverId}/members/{memberId}")
    public ResponseEntity<Map<String, String>> removeMemberFromServer(@RequestHeader("X-User-Id") String userId,
                                                                      @PathVariable("serverId") String serverId,
                                                                      @PathVariable("memberId") String memberId) {
        serverService.removeMemberFromServer(userId, serverId, memberId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "User deleted from server"));
    }

    @DeleteMapping(path = "/{serverId}/channel/{channelId}/user/{userId}")
    public ResponseEntity<Boolean> removeChannelFromServer(@PathVariable("userId") String userId,
                                                        @PathVariable("serverId") String serverId,
                                                        @PathVariable("channelId") String channelId) {
        Boolean isDeleted = serverService.removeChannelFromServer(userId, serverId, channelId);
        return ResponseEntity.status(HttpStatus.OK).body(isDeleted);
    }

}