package com.chatapp.server_service.client;

import com.chat_app.common_library.response.MemberDetailsResponse;
import com.chatapp.server_service.config.AuthServiceClientFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "auth-service", path = "/api/users", configuration = AuthServiceClientFeignConfig.class)
public interface UserServiceClient {
    @PostMapping(value = "details-by-ids")
    List<MemberDetailsResponse> getUsersDetailsByIds(@RequestBody List<String> userIds);
}
