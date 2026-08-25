package com.ec01.vo.category;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private Long productCount;
    private List<CategoryVO> children;
}
