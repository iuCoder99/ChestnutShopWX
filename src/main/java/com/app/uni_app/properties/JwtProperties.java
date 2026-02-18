package com.app.uni_app.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端员工生成 jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * 用户端微信用户生成 jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

    /**
     * redis 存储用户信息(角色..权限)过期时间
     */
    private long loginUserInfoInRedisTtl;

    /***
     * redis 存储刷新 token 过期时间
     */
    private long loginRefreshTokenTtl;


}

