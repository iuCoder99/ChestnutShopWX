package com.app.uni_app.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 购物车商品类
 */
@Data
public class CartItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 购物车 ID
     */
    private String id;
    /**
     * 用户 ID
     */
    private String userId;
    /**
     * 商品 ID
     */
    private String productId;
    /**
     * 规格 ID
     */
    private String specId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 单价
     */
    private Double price;
    /**
     * 库存数量
     */
    private Integer stock;
    /**
     * 商品名称
     */
    private String productName;
    /**
     * 商品图片 URL
     */
    private String productImage;
    /**
     * 规格文本描述
     */
    private String specText;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;


}