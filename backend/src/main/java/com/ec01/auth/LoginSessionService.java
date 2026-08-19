package com.ec01.auth;

public interface LoginSessionService {

    String createSession(Long userId);

    Long getUserId(String sessionId);

    boolean exists(String sessionId);

    void deleteSession(String sessionId);
}
