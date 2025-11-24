package com.chat_app.channel_service.client;

import com.chat_app.channel_service.config.AuthServiceClientFeignConfig;
import com.chat_app.channel_service.config.FeignConfig;
import com.chat_app.common_library.response.MemberDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "auth-service", path = "/api/users", configuration = {FeignConfig.class, AuthServiceClientFeignConfig.class})
public interface UserServiceClient {
    @GetMapping("/{userId}")
    void checkUserExistence(@PathVariable("userId") String userId);

    @PostMapping(value = "details-by-ids")
    List<MemberDetailsResponse> getUsersDetailsByIds(@RequestBody List<String> userIds);
}
