package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserRoleEnum {
    ROLE_ADMIN( 1,"系统管理员"),
    ROLE_BUYER(4,"普通买家");

    @EnumValue
    private final int id;

    private  final String roleName;


    UserRoleEnum(int id,String roleName){
        this.id=id;
        this.roleName=roleName;
    }


}
