package com.chatapp.api_gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/is-blacklisted",
            "/auth/validate-token"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints.stream()
                    .noneMatch(uri -> request.getURI().getPath().equalsIgnoreCase(uri));
}
