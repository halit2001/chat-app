package com.chatapp.auth_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
@Builder
public class User {
    @Id
    private String userId;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;
    private List<Role> role;
}
