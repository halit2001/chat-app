package com.chat_app.channel_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;

public class AuthServiceClientFeignConfig {
    @Value("${internal.api-key}")
    private String internalApiKey;

    public RequestInterceptor internalApiRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Internal-Api-Key", internalApiKey);
        };
    }
}
