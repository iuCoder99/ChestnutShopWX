package com.app.uni_app.pojo.entity;

import com.app.uni_app.pojo.emums.EnterpriseAuthStatus;
import com.app.uni_app.pojo.emums.UserType;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户表（个人/企业用户）实体类
 *
 * @author auto-generated
 * @date 2025-12-26
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName(value = "user")
public class User {

    /**
     * 用户ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（账号登录用）
     */
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String username;

    /**
     * 加密密码（BCrypt，微信登录用户可为空）
     */
    private String password;

    /**
     * 用户昵称
     */
    @TableField(value = "nickname", fill = FieldFill.INSERT)
    private String nickname = "";

    /**
     * 用户头像 URL
     */
    @TableField(value = "avatar", fill = FieldFill.INSERT)
    private String avatar = "/static/images/default-avatar.png";

    /**
     * 手机号（唯一）
     */
    private String phone;

    /**
     * 微信openid（小程序唯一标识，唯一）
     */
    private String openid;

    /**
     * 用户类型（个人/企业）
     */
    @TableField(value = "user_type", fill = FieldFill.INSERT)
    private UserType userType = UserType.PERSONAL;

    /**
     * 企业名称（企业用户）
     */
    private String enterpriseName;

    /**
     * 统一社会信用代码（企业用户，18位）
     */
    private String creditCode;

    /**
     * 营业执照图片URL（企业用户）
     */
    private String licenseUrl;

    /**
     * 企业认证状态（0-未认证，1-已认证，2-认证中，3-认证失败）
     */
    @TableField(value = "is_enterprise_auth", fill = FieldFill.INSERT)
    private Integer isEnterpriseAuth = 0;

    @TableField(exist = false)
    private com.app.uni_app.pojo.emums.EnterpriseAuthStatus enterpriseAuthStatus;
    /**
     * 备注（管理员添加）
     */
    private String remark;

    /**
     * 首次登录时间
     */
    private LocalDateTime firstLoginTime;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLoginTime;

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


    public com.app.uni_app.pojo.emums.EnterpriseAuthStatus getEnterpriseAuthStatus() {
        // 调用你枚举中改名后的方法
        return com.app.uni_app.pojo.emums.EnterpriseAuthStatus.getDesc(this.isEnterpriseAuth);
    }


}