package com.chatapp.auth_service.service.redis;

import com.chatapp.auth_service.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private JedisPooled jedis;

    @Override
    public String addTokenToBlacklist(String token) {
        long expiration = jwtTokenProvider.getExpirationFromToken(token);
        long current = System.currentTimeMillis();
        long ttl = (expiration - current) / 1000;
        jedis.setex(token, (int) ttl, "blacklisted");
        return "Token has been blacklisted for " + ttl + " seconds.";
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return jedis.exists(token);
    }
}
