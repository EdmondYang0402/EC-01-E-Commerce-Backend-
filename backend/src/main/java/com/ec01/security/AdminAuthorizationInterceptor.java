package com.ec01.security;

import com.ec01.common.UserRole;
import com.ec01.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object role = request.getAttribute(JwtInterceptor.AUTH_USER_ROLE_ATTRIBUTE);
        if (role != UserRole.ADMIN) {
            throw new ForbiddenException("需要管理员权限");
        }
        return true;
    }
}
