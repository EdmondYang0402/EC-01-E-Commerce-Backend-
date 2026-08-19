package com.ec01.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    DISABLED((byte) 0),
    NORMAL((byte) 1);

    private final byte code;
}
