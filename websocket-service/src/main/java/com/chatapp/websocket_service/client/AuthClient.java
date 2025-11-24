package com.chatapp.websocket_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "auth-service" , path = "/auth")
public interface AuthClient {

    @GetMapping("/validate-token")
    Map<String, Object> validateToken(@RequestParam("token") String token);
}
