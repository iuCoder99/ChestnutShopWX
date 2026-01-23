package com.app.uni_app.pojo.vo;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ProductVO {
    /**
     * 商品 ID
     */
    private String id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品图片 URL
     */
    private String image;

    /**
     * 商品价格
     */
    private Number price;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 分类 ID
     */
    private String categoryId;

    /**
     * 商品状态
     * 枚举值：active（启用）、inactive（禁用）
     */
    private CommonStatus status;

    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;

    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime updateTime;
}
