package com.app.uni_app.common.result;

import com.app.uni_app.pojo.emums.EnterpriseAuthStatus;
import com.app.uni_app.pojo.emums.UserType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    /**
     * 用户 ID
     */
    private String id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像 URL
     */
    private String avatar;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 微信用户唯一标识
     */
    private String openid;

    /**
     * 用户类型（枚举：PERSONAL/ENTERPRISE）
     */
    private UserType userType;

    /**
     * 企业认证状态
     */
    private Integer isEnterpriseAuth;

    /**
     * 企业认证状态详情（枚举：unauth/authing/authsuccess/authfailed）
     */
    private EnterpriseAuthStatus enterpriseAuthStatus;


}