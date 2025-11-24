package com.chatapp.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfig {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6380;

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled(REDIS_HOST, REDIS_PORT);
    }

}
