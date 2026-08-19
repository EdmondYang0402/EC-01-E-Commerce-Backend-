package com.ec01.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkuStatus {
    DISABLED((byte) 0),
    ENABLED((byte) 1);

    private final byte code;
}
