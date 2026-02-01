package com.app.uni_app.pojo.entity;

import com.app.uni_app.pojo.emums.CommonStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统角色表
 *
 * @author 开发者
 * @since 2026-01-26
 */
@Data
public class SysRole {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 角色编码（唯一，Shiro规范：ROLE_开头）
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 排序（前端展示）
     */
    private Integer sort;

    /**
     * 是否启用 1-启用 0-禁用
     */
    private CommonStatus isEnable;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}