package com.app.uni_app.security.realm;

import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.exception.InvalidCredentialsException;
import com.app.uni_app.common.result.UserInfo;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.entity.SysPermission;
import com.app.uni_app.pojo.entity.SysRole;
import com.app.uni_app.pojo.entity.SysUser;
import com.app.uni_app.security.token.JwtToken;
import io.jsonwebtoken.Claims;
import lombok.Setter;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
public class CustomRealm extends AuthorizingRealm {
    private static final Logger log = LoggerFactory.getLogger(CustomRealm.class);

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) authenticationToken;
        String token = (String) jwtToken.getCredentials();
        String userId = (String) jwtToken.getPrincipal();
        Claims claims = jwtToken.getClaims(); // 直接获取 Filter 解析好的 Claims
        // 验证 userId 是否一致（双重校验）
        String jwtUserId = claims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString();
        if (!userId.equals(jwtUserId)) {
            log.error("Token篡改：传入userId={}，JWT解析userId={}", userId, jwtUserId);
            throw new InvalidCredentialsException(MessageConstant.TOKEN_INVALID);
        }

        // 查询用户（带角色和权限）
        Map<String, Object> userMap = RedisConnector.opsForHash().entries(RedisKeyGenerator.loginUser(Long.parseLong(userId)));

        if (userMap.isEmpty()) {
            log.error("用户不存在：userId={}", userId);
            throw new UnknownAccountException(MessageConstant.ACCOUNT_NOT_FOUND);

        }

        if (!userMap.get(SysUser.Fields.isEnable).equals(CommonStatus.ACTIVE.getNumber())) {
            throw new DisabledAccountException(MessageConstant.ACCOUNT_LOCKED);

        }

        List<SysRole> sysRoleList = (List<SysRole>) userMap.get(SysUser.Fields.sysRoleList);
        List<SysPermission> sysPermissionList = (List<SysPermission>) userMap.get(SysUser.Fields.sysPermissionList);
        UserInfo userInfo = (UserInfo) userMap.get(SysUser.Fields.userInfo);

        if (Objects.isNull(sysRoleList) || Objects.isNull(sysPermissionList) || Objects.isNull(userInfo)) {
            throw new UnknownAccountException(MessageConstant.USER_NOT_LOGIN);

        }
        SysUser user = SysUser.builder()
                .id(Long.valueOf(userId))
                .sysRoleList(sysRoleList)
                .sysPermissionList(sysPermissionList)
                .userInfo(userInfo)
                .build();

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

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setRoles(roleNames);
        authorizationInfo.setStringPermissions(permNames);

        // 注意：doGetAuthorizationInfo 只有在鉴权（@RequiresRoles）时才会被调用
        // 对于普通接口，认证通过后并不会自动调用这里，所以 BaseContext 必须在认证阶段（doGetAuthenticationInfo）或 Filter 中设置
        return authorizationInfo;
    }
}