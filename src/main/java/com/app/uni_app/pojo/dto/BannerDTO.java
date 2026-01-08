package com.app.uni_app.pojo.dto;

import com.app.uni_app.pojo.emums.BannerStatus;
import lombok.Data;

/**
 * Banner 数据传输对象
 */
@Data
public class BannerDTO {

    /**
     * 标题
     */
    private String title;

    /**
     * 图片 URL
     */
    private String imageUrl;

    /**
     * 链接 URL
     */
    private String linkUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（枚举值：active=启用；inactive=禁用）
     */
    private BannerStatus status;
}