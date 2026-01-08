package com.app.uni_app.pojo.entity;

import com.app.uni_app.pojo.emums.CommonStatus;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统公告表实体类
 * 对应数据库表：notice
 */
@Data
@TableName("notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L; // 序列化版本号，避免序列化时出现版本冲突

    /**
     * 公告ID（主键）
     * 对应数据库字段：id，类型bigint，自增主键
     */
    @TableId(type = IdType.AUTO) // 指定主键类型为自增，匹配数据库表的auto_increment配置
    private Long id;

    /**
     * 公告标题
     * 对应数据库字段：title，类型varchar(100)，非空
     */
    private String title;

    /**
     * 公告内容（富文本）
     * 对应数据库字段：content，类型text，非空
     */
    private String content;

    /**
     * 状态（0-禁用，1-启用）
     * 对应数据库字段：status，类型tinyint(1)，默认值1，非空
     */
    private CommonStatus status=CommonStatus.ACTIVE;

    /**
     * 创建时间
     * 对应数据库字段：create_time，类型datetime，默认值CURRENT_TIMESTAMP，非空
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(style = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 对应数据库字段：update_time，类型datetime，默认值CURRENT_TIMESTAMP，更新时自动刷新，非空
     */
    @DateTimeFormat(style = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}