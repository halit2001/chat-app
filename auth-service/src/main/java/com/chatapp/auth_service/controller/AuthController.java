package com.chatapp.auth_service.controller;

import com.chatapp.auth_service.dto.LoginRequest;
import com.chatapp.auth_service.dto.RegisterRequest;
import com.chatapp.auth_service.exceptions.UserAlreadyExistsException;
import com.chatapp.auth_service.exceptions.UserNotFoundException;
import com.chatapp.auth_service.exceptions.UsernameAlreadyExistsException;
import com.chatapp.auth_service.mapper.UserMapper;
import com.chatapp.auth_service.model.User;
import com.chatapp.auth_service.security.JwtTokenProvider;
import com.chatapp.auth_service.service.redis.RedisService;
import com.chatapp.auth_service.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.tokens.Token;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    private final UserMapper userMapper;

    @Autowired
    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping(path = "hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello from auth-service");
    }

    @GetMapping(path = "/is-blacklisted")
    public ResponseEntity<?> isTokenBlacklisted(@RequestParam("token") String token) {
        boolean isBlacklisted = redisService.isTokenBlacklisted(token);
        return ResponseEntity.ok(Map.of("blacklisted", isBlacklisted));
    }

    @GetMapping(path = "/validate-token")
    public Map<String, Object> validateToken(@RequestParam("token") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                response.put("valid", false);
            } else {
                response.put("valid", true);
            }
        } catch (Exception e) {
            response.put("valid", false);
        }
        return response;
    }

    @PostMapping(path = "/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateJwtToken(authentication);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (UserNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(path = "/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) throws Exception {
        try {
            User user = userService.saveUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.convertRegisteredUserToResponse(user));
        } catch (UserAlreadyExistsException | UsernameAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // reset-password must be here.

}
