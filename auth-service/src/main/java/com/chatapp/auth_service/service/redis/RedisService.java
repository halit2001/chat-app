package com.chatapp.auth_service.service.redis;

public interface RedisService {
    String addTokenToBlacklist(String token);

    boolean isTokenBlacklisted(String token);
}
