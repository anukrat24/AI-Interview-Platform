package com.aiprep.interview.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String configuredSecret;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            // Generates a random key on startup if none is configured.
            // Fine for local dev; in production ALWAYS set JWT_SECRET so tokens
            // survive app restarts and work across multiple instances.
            log.warn("JWT_SECRET is not set - generating a random signing key for this run only. " +
                    "Set the JWT_SECRET env var in production so tokens remain valid across restarts.");
            this.signingKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        } else {
            byte[] keyBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : pad(keyBytes));
        }
    }

    private byte[] pad(byte[] input) {
        byte[] padded = new byte[32];
        System.arraycopy(input, 0, padded, 0, Math.min(input.length, 32));
        return padded;
    }

    public String generateToken(String email, List<String> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }
}
