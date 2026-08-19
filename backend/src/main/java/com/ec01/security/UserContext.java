package com.ec01.security;

import java.util.Objects;

public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        CURRENT_USER.set(Objects.requireNonNull(userId, "userId must not be null"));
    }

    public static Long get() {
        return CURRENT_USER.get();
    }

    public static void remove() {
        CURRENT_USER.remove();
    }
}
