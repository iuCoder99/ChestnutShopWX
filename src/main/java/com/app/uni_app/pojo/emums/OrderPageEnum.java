package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 订单页面枚举
 */
@Getter
public enum OrderPageEnum {
    /**
     * 全部订单页面
     */
    ALL("allPage", 1, "全部"),
    /**
     * 待付款订单页面
     */
    PENDING_PAYMENT("pendingPayPage", 2, "待付款"),
    /**
     * 打包中订单页面
     */
    PACKAGING("packagingPage", 3, "打包中"),
    /**
     * 待收货订单页面
     */
    PENDING_RECEIPT("pendingReceiptPage", 4, "待收货"),
    /**
     * 待评价订单页面
     */
    PENDING_EVALUATION("pendingEvalPage", 5, "评价");

    /**
     * 页面标识（
     */
    @JsonValue
    private final String pageKey;

    /**
     * 页面编码
     */
    @EnumValue
    private final int pageCode;

    /**
     * 页面显示名称
     */
    private final String pageName;

    OrderPageEnum(String pageKey, int pageCode, String pageName) {
        this.pageKey = pageKey;
        this.pageCode = pageCode;
        this.pageName = pageName;
    }

    /**
     * 按页面标识解析枚举
     */
    @JsonCreator
    public static OrderPageEnum getByPageKey(String pageKey) {
        for (OrderPageEnum pageEnum : OrderPageEnum.values()) {
            if (StringUtils.equals(pageEnum.pageKey, pageKey)) {
                return pageEnum;
            }
        }
        throw new IllegalArgumentException("无效的OrderPageEnum.pageKey:" + pageKey);
    }

    /**
     * 按页面编码解析枚举
     */
    public static OrderPageEnum getByPageCode(int pageCode) {
        for (OrderPageEnum pageEnum : OrderPageEnum.values()) {
            if (pageEnum.pageCode == pageCode) {
                return pageEnum;
            }
        }
        throw new IllegalArgumentException("无效的OrderPageEnum.pageCode:" + pageCode);
    }
}


