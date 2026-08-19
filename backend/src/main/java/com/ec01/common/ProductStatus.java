package com.ec01.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    OFF_SHELF((byte) 0),
    ON_SHELF((byte) 1);

    private final byte code;
}
