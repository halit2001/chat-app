package com.chatapp.auth_service.mapper;

import com.chatapp.auth_service.model.User;
import com.chat_app.common_library.response.MemberDetailsResponse;
import com.chatapp.auth_service.response.RegisteredUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public RegisteredUserResponse convertRegisteredUserToResponse(User user) {
        return RegisteredUserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .role(user.getRole())
                .build();
    }

    public MemberDetailsResponse convertUsersToMemberDetailsResponse(User user) {
        return MemberDetailsResponse.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
