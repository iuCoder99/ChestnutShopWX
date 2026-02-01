package com.app.uni_app.pojo.entity;

import com.app.uni_app.pojo.emums.CommonStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户表
 *
 * @since 2026-01-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName(value = "sys_user") // 指定数据库表名
public class SysUser  {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO) // 对应数据库的 auto_increment 自增策略
    private Long id;

    /**
     * 登录名
     */
    private String username;

    /**
     * 密码（JBCrypt加密，固定60位）
     */
    private String password;


    /**
     * 用户昵称
     */
    @TableField(value = "nickname")
    private String nickname = "";

    /**
     * 用户头像 URL
     */
    @TableField(value = "avatar")
    private String avatar = "/static/images/default-avatar.png";


    /**
     * 微信openid（小程序唯一标识，唯一）
     */
    private String openid;

    /**
     * 手机号
     */
    private String phone;


    /**
     * 电商用户类型 1-系统管理员 2-普通管理员 3-普通买家
     */
    @TableField(value = "user_type")
    private Byte userType;


    /**
     * 是否启用 1-启用 0-禁用（禁用后无法登录）
     */
    @TableField(value = "is_enable")
    private CommonStatus isEnable;

    /**
     * 角色列表
     */
    @TableField(exist = false)
    private List<SysRole> sysRoleList;


    /**
     * 权限列表
     */
    @TableField(exist = false)
    private List<SysPermission> sysPermissionList;

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
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime;

}