package com.app.uni_app.mapper;

import com.app.uni_app.pojo.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    SysUser getSysUserByNameWithRolesAndPermissions(String username);

    SysUser getSysUserByUserIdWithRolesAndPermissions(Long userId);
}


