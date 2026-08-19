package com.ec01.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Service
public class LoginSessionServiceImpl implements LoginSessionService {

    private static final String SESSION_KEY_PREFIX = "login:session:";

    private final StringRedisTemplate redisTemplate;
    private final Duration sessionTtl;

    public LoginSessionServiceImpl(StringRedisTemplate redisTemplate,
                                   @Value("${login.session.ttl:7200}") long sessionTtlSeconds) {
        if (sessionTtlSeconds <= 0) {
            throw new IllegalStateException("login.session.ttl must be greater than zero");
        }
        this.redisTemplate = redisTemplate;
        this.sessionTtl = Duration.ofSeconds(sessionTtlSeconds);
    }

    @Override
    public String createSession(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(key(sessionId), userId.toString(), sessionTtl);
        return sessionId;
    }

    @Override
    public Long getUserId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        String value = redisTemplate.opsForValue().get(key(sessionId));
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    public boolean exists(String sessionId) {
        return sessionId != null
                && !sessionId.isBlank()
                && Boolean.TRUE.equals(redisTemplate.hasKey(key(sessionId)));
    }

    @Override
    public void deleteSession(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            redisTemplate.delete(key(sessionId));
        }
    }

    private String key(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }
}
