package com.app.uni_app.pojo.entity;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 订单明细表实体（MyBatis-Plus）
 */
@Data
@TableName("order_item")
@Accessors(chain = true)
public class OrderItem {

    /**
     * 明细ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID（外键，非空）
     */
    private Long orderId;

    /**
     * 关联商品ID（外键，非空）
     */
    private Long productId;

    /**
     * 关联规格ID（外键，可空）
     */
    private Long specId;

    /**
     * 商品名称（下单时快照，非空）
     */
    private String productName;

    /**
     * 规格描述（下单时快照，可空）
     */
    private String specText;

    /**
     * 商品图片（下单时快照，可空）
     */
    private String productImage;

    /**
     * 购买单价（下单时价格，非空）
     */
    private BigDecimal price;

    /**
     * 购买数量（非空）
     */
    private Integer quantity;

    /**
     * 小计金额（单价×数量，非空）
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private BigDecimal subtotal;

    /**
     * 创建时间（默认当前时间，非空）
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;

    public BigDecimal getSubtotal() {
        if (Objects.nonNull(this.price)&&Objects.nonNull(this.quantity)){
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return null;
    }
}
