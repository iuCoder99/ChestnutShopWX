package com.app.uni_app.pojo.entity;

import com.app.uni_app.pojo.emums.CommonStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统权限表（电商三级）
 *
 * @author 开发者
 * @since 2026-01-26
 */
@Data
public class SysPermission {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 权限编码（唯一，三级结构：模块:功能:操作）
     */
    private String permCode;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 权限类型 1-菜单权限 2-按钮/接口权限
     */
    private Byte permType;

    /**
     * 父权限ID（0为顶级）
     */
    private Long parentId;

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

    // 可选：常量定义（方便业务使用）
    public static final Byte PERM_TYPE_MENU = 1;    // 菜单权限
    public static final Byte PERM_TYPE_BUTTON = 2;  // 按钮/接口权限
    public static final Byte IS_ENABLE_YES = 1;     // 启用
    public static final Byte IS_ENABLE_NO = 0;      // 禁用
    public static final Long PARENT_ID_TOP = 0L;    // 顶级权限父 ID
}