package com.chatapp.websocket_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
    @Value("${chatapp.app.app_secret}")
    private String APP_SECRET;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(APP_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token).getBody();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", String.class);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }
}
