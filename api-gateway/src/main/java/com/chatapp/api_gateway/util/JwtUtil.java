package com.chatapp.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtUtil {
    private String APP_SECRET = "K9c8LmZ4pT3xRqWv7Ue5Hs2DnYbGvXa1ZjQkRuLmP0OsTnEq7MwHzCtAyBjVxYkZ";

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(APP_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts the claims (payload) from the JWT token.
     * @param token The JWT token.
     * @return The claims parsed from the token.
     */
    public Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token).getBody();
    }

    /**
     * Retrieves the username (subject) from the JWT token.
     * @param token The JWT token.
     * @return The username extracted from the token.
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Retrieves the authorities associated with the token.
     * @param token The JWT token.
     * @return An array of authorities extracted from the token.
     */
    public List<String> getAuthoritiesFromToken(String token) {
        Claims claims = getClaims(token);
        String authoritiesString = claims.get("authorities", String.class);
        return Arrays.asList(authoritiesString.split("&"));
    }

    /**
     * Retrieves the userId from the JWT token.
     * @param token The JWT token.
     * @return The userId extracted from the token.
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", String.class);
    }

}
