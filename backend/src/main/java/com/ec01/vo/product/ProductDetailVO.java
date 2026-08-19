package com.ec01.vo.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductDetailVO {
    private Long id;
    private String name;
    private String subtitle;
    private String description;
    private Long categoryId;
    private String coverUrl;
    private Byte status;
    private List<SkuVO> skus;
}
