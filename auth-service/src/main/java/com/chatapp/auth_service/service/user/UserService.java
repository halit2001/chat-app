package com.chatapp.auth_service.service.user;

import com.chatapp.auth_service.dto.RegisterRequest;
import com.chatapp.auth_service.model.User;
import com.chat_app.common_library.response.MemberDetailsResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findUserByEmail(String email);

    User saveUser(RegisterRequest registerRequest);

    List<MemberDetailsResponse> createUserDetailsList(List<String> userIds);

    void checkUserExistence(String userId);
}
