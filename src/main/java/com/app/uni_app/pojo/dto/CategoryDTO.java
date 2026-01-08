package com.app.uni_app.pojo.dto;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class CategoryDTO {
    /**
     * 分类名称
     */
    private String name;
    /**
     * 父分类 id
     */
    private String parentId;
    /**
     * 排序
     */
    private Integer sort;

    private String iconUrl;
    /**
     * 1-启用 0-禁用
     */
    private Integer status;
}
