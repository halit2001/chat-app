package com.chatapp.auth_service.security;

import com.chatapp.auth_service.exceptions.UserNotFoundException;
import com.chatapp.auth_service.model.User;
import com.chatapp.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {
    @Autowired
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found with username: " + authentication.getName()));
        if (passwordEncoder().matches(authentication.getCredentials().toString(), user.getPassword())) {
            JwtUserDetails userDetails = JwtUserDetails.create(user);
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }
        throw new BadCredentialsException("Wrong Password");
    }
}
