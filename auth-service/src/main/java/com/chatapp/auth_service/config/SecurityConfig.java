package com.chatapp.auth_service.config;

import com.chatapp.auth_service.security.CustomAuthenticationManager;
import com.chatapp.auth_service.security.InternalApiKeyAuthFilter;
import jakarta.ws.rs.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationManager customAuthenticationManager;

    @Autowired
    public SecurityConfig(CustomAuthenticationManager customAuthenticationManager) {
        this.customAuthenticationManager = customAuthenticationManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/hello", "/auth/is-blacklisted", "/auth/validate-token").permitAll()
                        .requestMatchers("/api/users/**").hasRole("INTERNAL_SERVICE")
                        .anyRequest().authenticated())
                .addFilterBefore(new InternalApiKeyAuthFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    //CustomAuthenticationManager needs to be added here.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return customAuthenticationManager;
    }

}
