package com.ec01.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING_PAYMENT((byte) 0),
    PAID((byte) 1),
    SHIPPED((byte) 2),
    COMPLETED((byte) 3),
    CANCELLED((byte) 4);

    private final byte code;
}
