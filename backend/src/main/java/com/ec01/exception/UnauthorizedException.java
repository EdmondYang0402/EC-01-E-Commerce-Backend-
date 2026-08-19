package com.ec01.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(401, "Unauthorized");
    }
}
