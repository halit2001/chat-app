package com.chatapp.api_gateway.filter;

import com.chatapp.api_gateway.util.JwtUtil;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    @Autowired
    private RouteValidator validator;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return handleUnauthorized(exchange, "Missing authorization header.");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return handleUnauthorized(exchange, "Invalid or missing authorization header.");
            }

            String token = authHeader.substring(7);
            WebClient client = webClientBuilder.build();

            return client.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("auth-service")
                            .path("/auth/validate-token")
                            .queryParam("token", token)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .flatMap(responseMap -> {
                        Boolean valid = (Boolean) responseMap.get("valid");
                        if (Boolean.TRUE.equals(valid)) {
                            String userId = jwtUtil.getUserIdFromToken(token);
                            String username = jwtUtil.getUsernameFromToken(token);
                            List<String> authorities = jwtUtil.getAuthoritiesFromToken(token);

                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Username", username)
                                    .header("X-Authorities", String.join(",", authorities))
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } else {
                            return handleUnauthorized(exchange, "Invalid token.");
                        }
                    }).onErrorResume(e -> {
                        e.printStackTrace();
                        return handleUnauthorized(exchange, "Token validation failed.");
                    });
        }
        return chain.filter(exchange);
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Error", message);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
