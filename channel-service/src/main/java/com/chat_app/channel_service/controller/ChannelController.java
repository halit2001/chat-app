package com.chat_app.channel_service.controller;

import com.chat_app.channel_service.request.ChannelUpdateRequest;
import com.chat_app.channel_service.request.CreateChannelRequest;
import com.chat_app.channel_service.response.ChannelResponse;
import com.chat_app.channel_service.service.ChannelService;
import com.chat_app.common_library.response.MemberDetailsResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channel")
public class ChannelController {
    @Autowired
    private ChannelService channelService;

    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> getChannelInformation(@RequestHeader("X-User-Id") String userId,
                                                                 @PathVariable("channelId") String channelId) {
        ChannelResponse response = channelService.getChannelInformations(userId, channelId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = "/{channelId}/members")
    public ResponseEntity<List<MemberDetailsResponse>> getChannelMembers(@RequestHeader("X-User-Id") String userId,
                                                                         @PathVariable("channelId") String channelId) {
        List<MemberDetailsResponse> memberIds = channelService.getChannelMembers(userId, channelId);
        return ResponseEntity.status(HttpStatus.OK).body(memberIds);
    }

    @GetMapping(path = "/server/{serverId}")
    public ResponseEntity<List<ChannelResponse>> getChannelsByServerId(@RequestHeader("X-User-Id") String userId, @PathVariable("serverId") String serverId) {
        List<ChannelResponse> channelResponses = channelService.getChannelsByServerId(userId, serverId);
        return ResponseEntity.status(HttpStatus.OK).body(channelResponses);
    }

    @PostMapping(path = "/server/{serverId}")
    public ResponseEntity<ChannelResponse> createChannel(@RequestHeader("X-User-Id") String userId,
                                                         @PathVariable("serverId") String serverId,
                                                         @RequestBody @Valid CreateChannelRequest createChannelRequest) {
        ChannelResponse response = channelService.createChannel(userId, serverId, createChannelRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "{channelId}/member/{memberId}")
    public ResponseEntity<ChannelResponse> addMemberToChannel(@RequestHeader("X-User-Id") String userId,
                                                              @PathVariable("channelId") String channelId,
                                                              @PathVariable("memberId") String memberId) {
        ChannelResponse response = channelService.addMemberToChannel(userId, channelId, memberId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping(path = "/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(@RequestHeader("X-User-Id") String userId,
                                                         @PathVariable("channelId") String channelId,
                                                         @RequestBody @Valid ChannelUpdateRequest channelUpdateRequest) {
        ChannelResponse response = channelService.updateChannel(userId, channelId, channelUpdateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping(path = "/{channelId}")
    public ResponseEntity<Void> deleteChannel(@RequestHeader("X-User-Id") String userId,
                                              @PathVariable("channelId") String channelId) {
        channelService.deleteChannel(userId, channelId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
