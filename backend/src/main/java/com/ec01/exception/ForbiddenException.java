package com.ec01.exception;

public class ForbiddenException extends BusinessException {
    public ForbiddenException() {
        super(403, "Forbidden");
    }

    public ForbiddenException(String message) {
        super(403, message);
    }
}
