package com.chat_app.channel_service.service;

import com.chat_app.channel_service.client.ServerServiceClient;
import com.chat_app.channel_service.client.UserServiceClient;
import com.chat_app.channel_service.exceptions.ChannelNotFoundException;
import com.chat_app.channel_service.exceptions.UserAlreadyMemberChannelException;
import com.chat_app.channel_service.mapper.ChannelMapper;
import com.chat_app.channel_service.model.Channel;
import com.chat_app.channel_service.repository.ChannelRepository;
import com.chat_app.channel_service.request.ChannelUpdateRequest;
import com.chat_app.channel_service.request.CreateChannelRequest;
import com.chat_app.channel_service.response.ChannelResponse;
import com.chat_app.common_library.exceptions.AccessDeniedException;
import com.chat_app.common_library.response.MemberDetailsResponse;
import com.chat_app.common_library.response.ServerComprehensivePermissionsResponse;
import com.chat_app.common_library.response.UserServerPermissions;
import jakarta.ws.rs.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ChannelServiceImpl implements ChannelService {

    // Bağımlılıkları (Dependencies) constructor injection ile tanımlamak daha modern ve test edilebilir bir yaklaşımdır.
    private final ServerServiceClient serverServiceClient;
    private final ChannelMapper channelMapper;
    private final ChannelRepository channelRepository;
    private final UserServiceClient userServiceClient;

    @Autowired
    public ChannelServiceImpl(ServerServiceClient serverServiceClient, ChannelMapper channelMapper,
                              ChannelRepository channelRepository, UserServiceClient userServiceClient) {
        this.serverServiceClient = serverServiceClient;
        this.channelMapper = channelMapper;
        this.channelRepository = channelRepository;
        this.userServiceClient = userServiceClient;
    }

    // --- Servis Metotları ---

    @Override
    @Transactional
    public ChannelResponse createChannel(String userId, String serverId, CreateChannelRequest createChannelRequest) {
        // YENİ: Tekrarlanan kontrolü ayrı bir yardımcı metota ayırdık.
        verifyUserIsServerOwner(userId, serverId);

        Channel channel = channelMapper.createChannelModel(userId, serverId, createChannelRequest);

        try {
            channelRepository.save(channel);
            // Not: Feign/Microservice çağrıları transaction kapsamı dışında olmalıdır.
            serverServiceClient.addChannelToServer(serverId, channel.getId());
        } catch (Exception e) {
            // Eğer addChannelToServer başarısız olursa, işlemi geri al
            channelRepository.delete(channel);
            // ResourceNotFound veya ServiceCommunication zaten ErrorDecoder ile çevrildiyse,
            // sadece e'yi fırlatmak yeterlidir.
            throw new RuntimeException("Channel creation failed due to server-service communication.", e);
        }
        return channelMapper.convertChannelToResponse(channel);
    }

    @Override
    public ChannelResponse getChannelInformations(String userId, String channelId) {
        Channel channel = findChannelById(channelId);

        // YENİ: Erişim kontrolü mantığını bir metot içine aldık.
        verifyChannelAccess(userId, channel);

        return channelMapper.convertChannelToResponse(channel);
    }

    @Override
    @Transactional
    public ChannelResponse addMemberToChannel(String userId, String channelId, String memberId) {
        Channel channel = findChannelById(channelId);

        // YENİ: Karmaşık izin kontrolünü ayrı bir metoda ayırdık (SRP)
        validateChannelAdditionPreconditions(channel, userId, memberId);

        channel.getMembersIds().add(memberId);
        channelRepository.save(channel);

        return channelMapper.convertChannelToResponse(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsByServerId(String userId, String serverId) {
        // YENİ: Tüm izin ve kanal ID'si alma mantığı ayrı bir metotta (SRP)
        ServerComprehensivePermissionsResponse serverPermissions = getServerPermissions(serverId, userId);

        // YENİ: İzin kontrolü için ayrı metot (SRP)
        verifyUserServerAccess(serverPermissions, userId, serverId);

        List<String> channelIds = Optional.ofNullable(serverPermissions.getChannelIds()).orElse(Collections.emptyList());

        if (channelIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Channel> channels = channelRepository.findAllById(channelIds);
        return channels.stream().map(channelMapper::convertChannelToResponse).toList();
    }

    @Override
    @Transactional
    public ChannelResponse updateChannel(String userId, String channelId, ChannelUpdateRequest channelUpdateRequest) {
        Channel channel = findChannelAndVerifyOwner(userId, channelId);
        channelMapper.updateChannel(channel, channelUpdateRequest);
        channelRepository.save(channel);
        return channelMapper.convertChannelToResponse(channel);
    }

    @Override
    @Transactional
    public void deleteChannel(String userId, String channelId) {
        Channel channel = findChannelAndVerifyOwner(userId, channelId);

        // Hata yönetimini sadeleştirdik
        Boolean removedFromServer = serverServiceClient.removeChannelFromServer(userId, channel.getServerId(), channelId);

        if (!Boolean.TRUE.equals(removedFromServer)) {
            // Loglama burada yapılabilir, System.out.println yerine logger kullanmak daha iyidir
            // log.warn("Channel ID {} was not found...", channel.getId());
            System.out.println("Warning: Channel ID " + channel.getId() + " was not found in server " + channel.getServerId() + "'s channel list in server-service. Proceeding with local deletion.");
        }
        channelRepository.delete(channel);
    }

    @Override
    public List<MemberDetailsResponse> getChannelMembers(String userId, String channelId) {
        Channel channel = findChannelById(channelId);

        if (!"PRIVATE".equals(channel.getType().name())) {
            throw new BadRequestException("Channel is public. Membership list is not applicable."); // RuntimeException yerine daha spesifik bir 4xx hatası
        }

        // YENİ: İzin kontrolü için ayrı metot (SRP)
        if (!serverServiceClient.hasServerAccess(channel.getServerId(), userId)) {
            throw new AccessDeniedException("User can not get members without server access.");
        }

        if (channel.getMembersIds().isEmpty()) {
            return Collections.emptyList();
        }

        return userServiceClient.getUsersDetailsByIds(channel.getMembersIds());
    }

    // --- YARDIMCI VE REFACTOR EDİLMİŞ METOTLAR (SRP) ---

    private Channel findChannelById(String channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + channelId));
    }

    private void verifyUserIsServerOwner(String userId, String serverId) {
        // Bu metot, sadece tek bir iş yapar: sahibini doğrular.
        Boolean isOwner = serverServiceClient.isOwnerOfServer(userId, serverId);
        if (!Boolean.TRUE.equals(isOwner)) {
            throw new AccessDeniedException("Only the owner of the server can perform this action.");
        }
    }

    private Channel findChannelAndVerifyOwner(String userId, String channelId) {
        Channel channel = findChannelById(channelId);
        verifyUserIsServerOwner(userId, channel.getServerId());
        return channel;
    }

    private void verifyChannelAccess(String userId, Channel channel) {
        // Bu metot, sadece kanal erişim mantığını barındırır.
        if (Boolean.TRUE.equals(channel.getIsPrivate())) {
            if (!channel.getMembersIds().contains(userId)) {
                throw new AccessDeniedException("You do not have access to this private channel.");
            }
        } else {
            // Feign hatasının ErrorDecoder ile çevrildiğini varsayıyoruz.
            Boolean hasServerAccess = serverServiceClient.hasServerAccess(channel.getServerId(), userId);
            if (!Boolean.TRUE.equals(hasServerAccess)) {
                throw new AccessDeniedException("You do not have access to this server.");
            }
        }
    }

    // addMemberToChannel metodu için yardımcı metot
    private void validateChannelAdditionPreconditions(Channel channel, String requestingUserId, String memberId) {
        if (!Boolean.TRUE.equals(channel.getIsPrivate())) {
            throw new BadRequestException("Members can only be added to private channels.");
        }

        // Üyenin varlığını kontrol et (Kullanıcı servisi çağrısı)
        userServiceClient.checkUserExistence(memberId);

        // Kapsamlı izinleri al (Tek Feign çağrısı ile verimi artırma)
        List<String> usersToQuery = List.of(requestingUserId, memberId);
        ServerComprehensivePermissionsResponse permsResponse = serverServiceClient.getComprehensiveServerPermissions(channel.getServerId(), usersToQuery);

        UserServerPermissions requestingUserPerms = permsResponse.getUserPermissions().get(requestingUserId);

        // Talep eden kullanıcının yetkisini kontrol et
        if (requestingUserPerms == null || (!requestingUserPerms.isOwner() && !channel.getCreatedBy().equals(requestingUserId))) {
            throw new AccessDeniedException("Only the channel creator or server owner/manager can add members.");
        }

        UserServerPermissions memberToAddPerms = permsResponse.getUserPermissions().get(memberId);

        // Eklenecek üyenin sunucu üyesi olup olmadığını kontrol et
        if (memberToAddPerms == null || !memberToAddPerms.isMember()) {
            throw new BadRequestException("User to be added is not a member of the channel's server.");
        }

        // Zaten üye olup olmadığını kontrol et
        if (channel.getMembersIds().contains(memberId)) {
            throw new UserAlreadyMemberChannelException("User is already a member of this channel.");
        }
    }

    // getChannelsByServerId metodu için izin kontrolü
    private void verifyUserServerAccess(ServerComprehensivePermissionsResponse serverPermissions, String userId, String serverId) {
        UserServerPermissions requestingUserPerms = serverPermissions.getUserPermissions().get(userId);
        if (requestingUserPerms == null || (!requestingUserPerms.isOwner() && !requestingUserPerms.isMember())) {
            throw new AccessDeniedException("User " + userId + " does not have access to server " + serverId);
        }
    }

    // getChannelsByServerId metodu için Feign çağrısını ayırma
    private ServerComprehensivePermissionsResponse getServerPermissions(String serverId, String userId) {
        try {
            return serverServiceClient.getComprehensiveServerPermissions(serverId, List.of(userId));
        } catch (Exception ex) {
            // Burada ErrorDecoder'ın çeviremediği beklenmedik hataları yakalarız
            throw new RuntimeException("Failed to get server permissions for server ID: " + serverId, ex);
        }
    }
}