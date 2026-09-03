package com.ec01.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING((byte) 0),
    SUCCESS((byte) 1),
    FAILED((byte) 2),
    CANCELLED((byte) 3);

    private final byte code;
}
