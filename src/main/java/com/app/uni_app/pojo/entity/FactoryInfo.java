package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工厂基础信息表实体类
 */
@Data
@TableName("factory_info")
public class FactoryInfo {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 单张背景图
     */
    private String image;

    /**
     * 工厂名称
     */
    private String factoryName;

    /**
     * 工厂简介
     */
    private String introduction;

    /**
     * 服务热线
     */
    private String serviceHotline;

    /**
     * 官方微信
     */
    private String officialWechat;

    /**
     * 工厂地址
     */
    private String address;

    /**
     * 版权信息
     */
    private String copyrightInfo;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}