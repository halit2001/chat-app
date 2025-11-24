package com.chatapp.auth_service.response;

import com.chatapp.auth_service.model.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredUserResponse {
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private List<Role> role;
}
