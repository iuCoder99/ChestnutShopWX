package com.app.uni_app.pojo.entity;

import com.app.uni_app.pojo.emums.BannerStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 首页轮播图表 实体类
 * 对应表：banner
 */
@Data // Lombok 注解，自动生成 getter/setter/toString/equals/hashCode 等方法
@TableName("banner") // 对应数据库表名（若实体类名与表名一致可省略）
public class Banner {

    /**
     * 轮播图ID（主键）
     * 主键策略：自增（对应数据库 auto_increment）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 轮播图标题
     * 数据库字段：title（允许为 null）
     */
    private String title;

    /**
     * 图片URL
     * 数据库字段：image_url（非 null）
     */
    private String imageUrl;

    /**
     * 跳转链接（如：/product/detail?productId=123）
     * 数据库字段：link_url（允许为 null）
     */
    private String linkUrl;

    /**
     * 链接类型（product、category、url等）
     * 数据库字段：link_type（允许为 null）
     */
    private String linkType;

    /**
     * 排序（数字越小越靠前）
     * 数据库字段：sort（默认值 0，非 null）
     */
    private Integer sort = 0;

    /**
     * 状态（0-禁用，1-启用）
     * 数据库字段：status（默认值 1，非 null）
     * 枚举自动映射
     */
    private BannerStatus status = BannerStatus.ACTIVE;

    /**
     * 创建时间
     * 数据库字段：create_time（默认 CURRENT_TIMESTAMP，非 null）
     * 自动填充策略：插入时填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 数据库字段：update_time（默认 CURRENT_TIMESTAMP，更新时自动刷新，非 null）
     * 自动填充策略：插入和更新时填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}