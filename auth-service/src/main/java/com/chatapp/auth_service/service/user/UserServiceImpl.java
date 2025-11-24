package com.chatapp.auth_service.service.user;

import com.chat_app.common_library.response.MemberDetailsResponse;
import com.chatapp.auth_service.dto.RegisterRequest;
import com.chatapp.auth_service.exceptions.UserAlreadyExistsException;
import com.chatapp.auth_service.exceptions.UserNotFoundException;
import com.chatapp.auth_service.exceptions.UsernameAlreadyExistsException;
import com.chatapp.auth_service.mapper.UserMapper;
import com.chatapp.auth_service.model.Role;
import com.chatapp.auth_service.model.User;
import com.chatapp.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) throw new UserAlreadyExistsException("User already exists with email: " + registerRequest.getEmail());
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) throw new UsernameAlreadyExistsException("User already exists with username: " + registerRequest.getUsername());
        return userRepository.save(User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .role(List.of(Role.USER))
                .build());
    }

    @Override
    public List<MemberDetailsResponse> createUserDetailsList(List<String> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        return users.stream().map(userMapper::convertUsersToMemberDetailsResponse).toList();
    }

    @Override
    public void checkUserExistence(String userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with userId: " + userId));
    }

}
