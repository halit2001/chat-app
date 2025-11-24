package com.chatapp.server_service.service;

import com.chat_app.common_library.response.ServerComprehensivePermissionsResponse;
import com.chat_app.common_library.response.UserServerPermissions;
import com.chatapp.server_service.client.MediaServiceClient;
import com.chatapp.server_service.client.UserServiceClient;
import com.chatapp.server_service.dto.AddMemberRequest;
import com.chatapp.server_service.dto.CreateServerDto;
import com.chatapp.server_service.dto.JoinRequestDto;
import com.chatapp.server_service.dto.UpdateServerDto;
import com.chatapp.server_service.exceptions.*;
import com.chat_app.common_library.exceptions.AccessDeniedException;
import com.chat_app.common_library.exceptions.ServerNotFoundException;
import com.chatapp.server_service.mapper.ServerMapper;
import com.chatapp.server_service.model.Server;
import com.chatapp.server_service.repository.ServerRepository;
import com.chatapp.server_service.response.CreatedServerResponse;
import com.chat_app.common_library.response.MemberDetailsResponse;
import com.chatapp.server_service.response.ServerDetailsResponse;
import com.chatapp.server_service.util.ServerPermissionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ServerServiceImpl implements ServerService {
    private static final Logger log = LoggerFactory.getLogger(ServerService.class);
    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private ServerPermissionUtil serverPermissionUtil;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ServerDetailsResponse getServerDetails(String userId, String serverId) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId : " + serverId));
        if (serverPermissionUtil.isOwner(server, userId) || serverPermissionUtil.isMember(server, userId))
            return serverMapper.convertServerModelToServerDetailsResponse(server);
        throw new AccessDeniedException("You are not a member or owner of this server.");
    }

    @Transactional
    @Override
    public CreatedServerResponse createNewServer(String userId, CreateServerDto createServerDto, MultipartFile file) throws IOException {
        Optional<Server> optionalServer = serverRepository.findByServerName(createServerDto.getServerName());
        optionalServer.ifPresent(server -> {
            throw new ServerAlreadyExistsException("Server already exists.");
        });
        String iconUrl = null;
        String cloudinaryPublicId = null;
        if (file != null && !file.isEmpty()) {
            try {
                Map<String, String> uploadResponse = mediaServiceClient.uploadIcon(file);
                iconUrl = uploadResponse.get("iconUrl");
                if (iconUrl == null || iconUrl.isEmpty()) {
                    throw new MediaServiceCommunicationException("File upload was successful but URL could not be retrieved. Please try again.");
                }
                cloudinaryPublicId = getPublicIdFromCloudinaryUrl(iconUrl);
                if (cloudinaryPublicId == null || cloudinaryPublicId.isEmpty()) {
                    throw new MediaServiceCommunicationException("Failed to extract Public ID from uploaded icon URL.");
                }
            } catch (FeignException exception) {
                handleMediaServiceFeignException(exception, "initial server icon upload", null);
            } catch (Exception e) {
                throw new RuntimeException("An unexpected error occurred during file upload.", e);
            }
        }
        Server server = serverMapper.createServer(userId, createServerDto, iconUrl, cloudinaryPublicId);
        serverRepository.save(server);
        return serverMapper.convertServerModelToCreatedServerResponse(server);
    }

    @Override
    public List<ServerDetailsResponse> getUserServers(String userId) {
        Query ownerQuery = new Query(Criteria.where("ownerId").is(userId));
        List<Server> ownerServers = mongoTemplate.find(ownerQuery, Server.class);

        Query memberQuery = new Query(Criteria.where("memberIds").in(userId));
        List<Server> memberServers = mongoTemplate.find(memberQuery, Server.class);

        List<Server> allUserServers = new ArrayList<>(ownerServers);
        for (Server server : memberServers) {
            if (!allUserServers.contains(server)) {
                allUserServers.add(server);
            }
        }
        return allUserServers.stream().map(server -> serverMapper.convertServerModelToServerDetailsResponse(server)).toList();
    }

    @Transactional
    @Override
    public ServerDetailsResponse updateServerDetails(String userId, String serverId, UpdateServerDto updateServerDto, MultipartFile file) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with id: " + serverId));
        if (!serverPermissionUtil.canManageServer(server, userId))
            throw new AccessDeniedException("User can not update server details");
        String newIconUrl = server.getIconUrl();
        if (file != null && !file.isEmpty()) {
            try {
                String existingPublicId = server.getCloudinaryPublicId();
                if (existingPublicId == null || existingPublicId.isEmpty())
                    existingPublicId = getPublicIdFromCloudinaryUrl(server.getIconUrl());
                if (existingPublicId == null || existingPublicId.isEmpty()) {
                    throw new MediaServiceCommunicationException(
                            "Cannot update icon: No existing icon ID found. " +
                                    "Please ensure server has an icon or try creating a new one if it's the first upload.");
                }
                try {
                    Map<String, String> updatedResponse = mediaServiceClient.updateIcon(file, existingPublicId, true);
                    newIconUrl = updatedResponse.get("iconUrl");
                    if (newIconUrl == null || newIconUrl.isEmpty()) {
                        throw new MediaServiceCommunicationException("File upload was successful but URL could not be retrieved. Please try again.");
                    }
                } catch (FeignException exception) {
                    handleMediaServiceFeignException(exception, "server icon update", serverId);
                }
            } catch (Exception e) {
                throw new RuntimeException("An unexpected error occurred during server icon update.", e);
            }
        }
        Optional.ofNullable(updateServerDto.getServerName()).ifPresent(server::setServerName);
        Optional.ofNullable(updateServerDto.getDescription()).ifPresent(server::setDescription);
        server.setIconUrl(newIconUrl);
        serverRepository.save(server);
        return serverMapper.convertServerModelToServerDetailsResponse(server);
    }

    @Transactional
    @Override
    public String addMemberToServer(String userId, String serverId, AddMemberRequest memberRequest) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId: " + serverId));
        if (!serverPermissionUtil.canManageServer(server, userId))
            throw new AccessDeniedException("User doesn't have access to add member");
        if (server.getMembersIds() == null) server.setMembersIds(new ArrayList<>());
        if (server.getMembersIds().contains(memberRequest.getMemberId()))
            throw new MemberAlreadyExistsException("Member already exists in server with memberId: " + memberRequest.getMemberId());
        server.getMembersIds().add(memberRequest.getMemberId());
        serverRepository.save(server);
        log.info("User {} added to server {} by user {}.", memberRequest.getMemberId(), serverId, userId);
        return memberRequest.getMemberId();
    }

    @Override
    public void removeMemberFromServer(String userId, String serverId, String memberId) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId: " + serverId));
        if (!serverPermissionUtil.canManageServer(server, userId) && server.getMembersIds().contains(userId))
            throw new AccessDeniedException("User doesn't have access to remove member");
        if (server.getMembersIds() == null || !server.getMembersIds().contains(memberId))
            throw new MemberNotFoundException("User with ID '" + memberId + "' is not a member of server " + serverId + ".");
        server.getMembersIds().remove(memberId);
        serverRepository.save(server);
        log.info("User {} removed from server {} by user {}.", memberId, serverId, userId);
    }

    @Override
    public List<MemberDetailsResponse> getMemberDetailsWithServer(String userId, String serverId) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId: " + serverId));
        if (!serverPermissionUtil.isMember(server, userId) && !serverPermissionUtil.isOwner(server, userId))
            throw new AccessDeniedException("User doesn't have access to view members");
        List<String> memberIds = server.getMembersIds();
        if (memberIds == null || memberIds.isEmpty()) return new ArrayList<>();
        try {
            return userServiceClient.getUsersDetailsByIds(memberIds);
        } catch (FeignException e) {
            log.error("Failed to fetch user details from auth-service for server {}: {}", serverId, e.getMessage());
            HttpStatus feignHttpStatus = HttpStatus.resolve(e.status());
            if (feignHttpStatus == null) {
                feignHttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            String errorMessage = String.format("Auth Service experienced an error (Status: %d). Details: %s", e.status(),
                    e.getMessage() != null ? e.getMessage() : "No specific message provided.");
            throw new AuthServiceCommunicationException(errorMessage, e, feignHttpStatus);
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching member details for server {}: {}", serverId, e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during member details retrieval.", e);
        }
    }

    @Transactional
    @Override
    public void joinServer(String userId, JoinRequestDto joinRequestDto) {
        String serverId = joinRequestDto.getServerId();
        if (serverId == null || serverId.isEmpty()) throw new IllegalArgumentException("serverId is not found");
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId: " + serverId));
        if (serverPermissionUtil.isOwner(server, userId) || serverPermissionUtil.isMember(server, userId))
            throw new MemberAlreadyExistsException("User already exists in the server");
        if (server.getMembersIds() == null) server.setMembersIds(new ArrayList<>());
        server.getMembersIds().add(userId);
        serverRepository.save(server);
    }

    @Override
    public Boolean isOwnerOfServer(String userId, String serverId) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId: " + serverId));
        return serverPermissionUtil.isOwner(server, userId);
    }

    @Override
    public Boolean hasServerAccess(String serverId, String userId) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with: " + serverId));
        return serverPermissionUtil.canSeeServer(server, userId);
    }

    @Override
    public Boolean isUserMemberOfServer(String serverId, String memberId) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId: " + serverId));
        return serverPermissionUtil.isMember(server, memberId);
    }

    @Override
    public ServerComprehensivePermissionsResponse getComprehensiveServerPermissions(String serverId, List<String> userIds) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with: " + serverId));
        return serverMapper.createServerComprehensivePermissionsResponse(server, userIds);
    }

    @Override
    @Transactional
    public void addChannelToServer(String serverId, String channelId) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId: " + serverId));
        if (server.getChannelIds() == null) server.setChannelIds(new ArrayList<>());
        if (!server.getChannelIds().contains(channelId)) {
            server.getChannelIds().add(channelId);
            serverRepository.save(server);
        }
    }

    @Override
    @Transactional
    public Boolean removeChannelFromServer(String userId, String serverId, String channelId) {
        Server server = serverRepository.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server not found with serverId: " + serverId));
        if (!serverPermissionUtil.isOwner(server, userId)) {
            throw new AccessDeniedException("User doesn't have access to delete channel");
        }
        Boolean isDeleted = server.getChannelIds().remove(channelId);
        serverRepository.save(server);
        return isDeleted;
    }

    private String getPublicIdFromCloudinaryUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return null;
        }
        try {
            int uploadIndex = cloudinaryUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }
            String pathAfterUpload = cloudinaryUrl.substring(uploadIndex + "/upload/".length());
            if (pathAfterUpload.matches("^v\\d+/.*")) {
                int firstSlashAfterVersion = pathAfterUpload.indexOf('/');
                if (firstSlashAfterVersion != -1) {
                    pathAfterUpload = pathAfterUpload.substring(firstSlashAfterVersion + 1);
                }
            }
            int dotIndex = pathAfterUpload.lastIndexOf('.');
            if (dotIndex != -1) {
                pathAfterUpload = pathAfterUpload.substring(0, dotIndex);
            }
            int queryParamIndex = pathAfterUpload.indexOf('?');
            if (queryParamIndex != -1) {
                pathAfterUpload = pathAfterUpload.substring(0, queryParamIndex);
            }
            return pathAfterUpload;
        } catch (Exception e) {
            return null;
        }
    }

    private void handleMediaServiceFeignException(FeignException exception, String contextMessage, String relatedEntityId) {
        String errorMessage = "An error occurred while communicating with the media service.";

        if (exception.responseBody().isPresent()) {
            ByteBuffer errorByteBuffer = exception.responseBody().get();
            byte[] errorBytes = new byte[errorByteBuffer.remaining()];
            errorByteBuffer.get(errorBytes);
            try {
                Map<String, String> errorMap = objectMapper.readValue(errorBytes, Map.class);
                if (errorMap != null && errorMap.containsKey("error")) {
                    errorMessage = "Media service error: " + errorMap.get("error");
                } else {
                    errorMessage = "Error response from media service: " + new String(errorBytes, StandardCharsets.UTF_8);
                }
            } catch (IOException ex) {
                errorMessage = "The error response from the media service is not JSON or is invalid: " + new String(errorBytes, StandardCharsets.UTF_8);
                log.error("Failed to parse media service error response for {}: {}", contextMessage, ex.getMessage(), ex);
            }
        } else {
            errorMessage = "Could not reach media service or no response was received.";
        }
        log.error("Feign error during {}{} for entity {}: {}", contextMessage, (relatedEntityId != null ? " (" + relatedEntityId + ")" : ""), errorMessage, exception);
        throw new MediaServiceCommunicationException(errorMessage, exception);
    }
}
