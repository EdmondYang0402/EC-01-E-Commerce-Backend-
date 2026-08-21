package com.ec01.vo.category;

import com.ec01.common.CategoryStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryAdminVO {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private CategoryStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
