package com.ec01.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;

@Component
public class JwtUtil {

    private static final String USER_ID_CLAIM = "userId";
    private static final String SESSION_ID_CLAIM = "sessionId";

    private final SecretKey signingKey;
    private final JwtParser jwtParser;
    private final long expirationMillis;


    public JwtUtil(@Value("${jwt.secret}") String encodedSecret,
                   @Value("${jwt.expiration}") long expirationMillis) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured through jwt.secret/JWT_SECRET");
        }
        if (expirationMillis <= 0) {
            throw new IllegalStateException("jwt.expiration must be greater than zero");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(encodedSecret);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "JWT secret must be a valid Base64 HMAC key of at least 256 bits", exception);
        }

        this.jwtParser = Jwts.parser().verifyWith(signingKey).build();
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(Long userId, String sessionId) {
        Objects.requireNonNull(userId, "userId must not be null");
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }

        Date issuedAt = new Date();
        Date expiration = new Date(Math.addExact(issuedAt.getTime(), expirationMillis));
        return Jwts.builder()
                .claim(USER_ID_CLAIM, userId)
                .claim(SESSION_ID_CLAIM, sessionId)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public Long parseUserId(String token) {
        Object userId = parseClaims(token).get(USER_ID_CLAIM);
        return userId instanceof Number number ? number.longValue() : null;
    }

    public String parseSessionId(String token) {
        return parseClaims(token).get(SESSION_ID_CLAIM, String.class);
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = parseClaims(token);
            Object userId = claims.get(USER_ID_CLAIM);
            String sessionId = claims.get(SESSION_ID_CLAIM, String.class);
            return userId instanceof Number && sessionId != null && !sessionId.isBlank();
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return claims.get("userId", Long.class);
    }
}
