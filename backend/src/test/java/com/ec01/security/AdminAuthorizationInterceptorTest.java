package com.ec01.security;

import com.ec01.common.UserRole;
import com.ec01.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAuthorizationInterceptorTest {
    private final AdminAuthorizationInterceptor interceptor = new AdminAuthorizationInterceptor();

    @Test
    void normalUserCannotAccessAdminApi() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(JwtInterceptor.AUTH_USER_ROLE_ATTRIBUTE, UserRole.USER);

        assertThrows(ForbiddenException.class, () -> interceptor.preHandle(
                request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void administratorCanAccessAdminApi() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(JwtInterceptor.AUTH_USER_ROLE_ATTRIBUTE, UserRole.ADMIN);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
}
