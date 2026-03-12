package com.app.uni_app.pojo.emums;


import com.app.uni_app.pojo.entity.ProductComment;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 商品评论排序类型枚举
 */
@Getter
public enum ProductCommentQuerySortTypeEnum {

    DEFAULT("default", null, null,"默认(全部)"),

    IS_GOOD_REVIEW("isGoodReview", ProductComment::getIsGoodReview,1, "是否好评"),

    IS_APPEND_COMMENT("isAppendComment", ProductComment::getIsAppendComment, 1,"是否追评");

    /**
     * 前端展示/传输值
     */
    @JsonValue
    private final String value;

    /**
     * 数据库存储编码
     */
    private final SFunction<ProductComment,Object> function;

    /**
     * 函数参数
     */
    private final Object parameter;

    /**
     * 中文描述
     */
    private final String desc;

    ProductCommentQuerySortTypeEnum(String value,SFunction<ProductComment,Object> function,Object parameter, String desc) {
        this.value = value;
        this.function = function;
        this.parameter = parameter;
        this.desc = desc;
    }

    /**
     * 根据 value 获取枚举
     */
    @JsonCreator
    public static ProductCommentQuerySortTypeEnum getByValue(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("ProductQuerySortTypeEnum.value 为 null");
        }
        for (ProductCommentQuerySortTypeEnum sortType : ProductCommentQuerySortTypeEnum.values()) {
            if (StringUtils.equals(value, sortType.value)) {
                return sortType;
            }
        }
        throw new IllegalArgumentException("无效的ProductQuerySortTypeEnum.value:" + value);
    }
}