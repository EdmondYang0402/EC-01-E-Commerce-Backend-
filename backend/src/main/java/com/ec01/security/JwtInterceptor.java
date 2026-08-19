package com.ec01.security;

import com.ec01.auth.JwtUtil;
import com.ec01.auth.LoginSessionService;
import com.ec01.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    public static final String AUTH_SESSION_ID_ATTRIBUTE = "com.ec01.auth.sessionId";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final LoginSessionService loginSessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserContext.remove();
        if (isPublicRequest(request)) {
            return true;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException();
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty() || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException();
        }

        Long jwtUserId;
        String sessionId;
        try {
            jwtUserId = jwtUtil.parseUserId(token);
            sessionId = jwtUtil.parseSessionId(token);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException();
        }

        Long sessionUserId = loginSessionService.getUserId(sessionId);
        if (jwtUserId == null || sessionUserId == null || !Objects.equals(jwtUserId, sessionUserId)) {
            throw new UnauthorizedException();
        }

        UserContext.set(jwtUserId);
        request.setAttribute(AUTH_SESSION_ID_ATTRIBUTE, sessionId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception exception) {
        UserContext.remove();
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        if (HttpMethod.POST.matches(method)) {
            return "/api/auth/register".equals(path) || "/api/auth/login".equals(path);
        }
        if (HttpMethod.GET.matches(method)) {
            return isPathOrChild(path, "/api/products")
                    || isPathOrChild(path, "/api/categories");
        }
        return false;
    }

    private boolean isPathOrChild(String path, String root) {
        return root.equals(path) || path.startsWith(root + "/");
    }
}
