package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MQ消费失败消息表 实体类
 *
 */
@Data
@TableName("mq_consumer_failed_msg")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MqConsumerFailedMsg {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * RocketMQ消息ID
     */
    @TableField("msg_id")
    private String msgId;

    /**
     * 业务ID（商品ID/订单ID）
     */
    @TableField("biz_id")
    private String bizId;

    /**
     * 主题
     */
    @TableField("topic")
    private String topic;

    /**
     * 标签
     */
    @TableField("tag")
    private String tag;

    /**
     * 消息体JSON
     */
    @TableField("body")
    private String body;

    /**
     * 已重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 异常信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 状态 0-待处理 1-已处理 2-无需处理
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
