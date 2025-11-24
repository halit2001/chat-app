package com.chat_app.channel_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new CustomFeignErrorDecoder(objectMapper);
    }
}
