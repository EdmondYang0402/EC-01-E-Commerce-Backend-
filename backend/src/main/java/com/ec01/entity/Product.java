package com.ec01.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Product {
    private Long id;
    private String name;
    private String subtitle;
    private String description;
    private Long categoryId;
    private String coverUrl;
    private Byte status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
