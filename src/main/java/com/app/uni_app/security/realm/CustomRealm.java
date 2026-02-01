package com.app.uni_app.security.realm;

import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.exception.InvalidCredentialsException;
import com.app.uni_app.common.result.UserInfo;
import com.app.uni_app.common.util.JwtUtils;
import com.app.uni_app.pojo.entity.SysPermission;
import com.app.uni_app.pojo.entity.SysRole;
import com.app.uni_app.pojo.entity.SysUser;
import com.app.uni_app.properties.JwtProperties;
import com.app.uni_app.security.token.JwtToken;
import com.app.uni_app.service.SysLoginService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.Setter;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
public class CustomRealm extends AuthorizingRealm {
    private static final Logger log = LoggerFactory.getLogger(CustomRealm.class);

    private SysLoginService sysLoginService;
    private JwtProperties jwtProperties;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) authenticationToken;
        String token = (String) jwtToken.getCredentials();
        String userId = (String) jwtToken.getPrincipal();
        Claims claims = jwtToken.getClaims(); // 直接获取 Filter 解析好的 Claims

        log.debug("Realm认证：userId={}, token={}", userId, token);

        // 验证 userId 是否一致（双重校验）
        String jwtUserId = claims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString();
        if (!userId.equals(jwtUserId)) {
            log.error("Token篡改：传入userId={}，JWT解析userId={}", userId, jwtUserId);
            throw new InvalidCredentialsException(MessageConstant.TOKEN_INVALID);
        }

        // 查询用户（带角色和权限）
        SysUser user = sysLoginService.getSysUserByUserIdWithRolesAndPermissions(Long.valueOf(userId));
        if (Objects.isNull(user)) {
            log.error("用户不存在：userId={}", userId);
            throw new UnknownAccountException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 填充 BaseContext（包含更多信息）
        UserInfo userInfo = UserInfo.builder()
                .id(userId)
                .nickname(user.getNickname())
                .openid(user.getOpenid())
                .avatar(user.getAvatar())
                .build();
        BaseContext.setUserInfo(userInfo);

        log.debug("用户认证成功：username={}", user.getUsername());
        return new SimpleAuthenticationInfo(user, token, this.getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SysUser user = (SysUser) principalCollection.getPrimaryPrincipal();
        if (Objects.isNull(user)) {
            log.error("授权失败：用户不存在");
            throw new UnknownAccountException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 提取角色和权限
        Set<String> roleNames = user.getSysRoleList().stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.toSet());
        Set<String> permNames = user.getSysPermissionList().stream()
                .map(SysPermission::getPermName)
                .collect(Collectors.toSet());

        log.debug("用户授权：username={}, roles={}, permissions={}", user.getUsername(), roleNames, permNames);
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setRoles(roleNames);
        authorizationInfo.setStringPermissions(permNames);
        return authorizationInfo;
    }
}
