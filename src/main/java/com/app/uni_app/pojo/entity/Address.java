package com.app.uni_app.pojo.entity;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.CommonDefault;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 收货地址表
 *
 */
@Data
@TableName("address")
public class Address {

    /**
     * 地址ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户 ID
     */
    private Long userId;

    /**
     * 收件人姓名
     */
    private String receiver;

    /**
     * 收件人手机号
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址（街道/门牌号）
     */
    private String detailAddress;

    /**
     * 是否默认地址（0-否，1-是）
     */
    private CommonDefault isDefault;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}