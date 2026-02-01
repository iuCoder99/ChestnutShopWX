package com.app.uni_app.security.token;


import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * 修复：Principal 存储用户ID，Credentials 存储 Token
 */
public class JwtToken implements AuthenticationToken {
    private final String userId; // 用户唯一标识
    private final String token;  // JWT令牌（凭证）
    private final Claims claims; // 解析后的 Claims

    // 构造器：从Token解析出userId后传入（解析逻辑在JwtFilter中）
    public JwtToken(String userId, String token, Claims claims) {
        this.userId = userId;
        this.token = token;
        this.claims = claims;
    }

    // 身份信息：用户ID
    @Override
    public Object getPrincipal() {
        return userId;
    }

    // 凭证信息：JWT令牌
    @Override
    public Object getCredentials() {
        return token;
    }

    public Claims getClaims() {
        return claims;
    }
}