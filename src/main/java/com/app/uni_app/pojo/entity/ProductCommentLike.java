package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("product_comment_like")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCommentLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("comment_id")
    private Long commentId;

    @TableField("user_id")
    private Long userId;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}