package com.app.uni_app.security.filter;

import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.util.JwtUtils;
import com.app.uni_app.pojo.entity.SysUser;
import com.app.uni_app.properties.JwtProperties;
import com.app.uni_app.security.token.JwtToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.DecodingException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Setter
public class JwtFilter extends AuthenticatingFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer";
    private static final String REFRESH_TOKEN_HEADER = "Access-Control-Expose-Headers";

    // 仅用setter注入（ShiroConfig中配置，避免@Resource冲突）
    private ObjectMapper objectMapper;
    private JwtProperties jwtProperties;
    private JwtUtils jwtUtils;
    private Cache<String, Claims> tokenWhitelistCache;

    /**
     * 预处理：跨域OPTIONS请求放行
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        // 跨域配置（宽松适配前端）
        httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", TOKEN_HEADER + ",Content-Type");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * 核心：解析Token，封装JwtToken（确保userId非空）
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(servletRequest);
        String authHeader = httpRequest.getHeader(TOKEN_HEADER);
        log.debug("获取到的Authorization头：{}", authHeader);

        // 无Token或格式错误，返回null
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.debug("Token 不存在或格式错误");
            return null;
        }

        // 提取纯 JWT令牌（彻底清理所有空白字符，包括中间的空格、换行等）
        String jwtToken = authHeader.substring(TOKEN_PREFIX.length()).replaceAll("\\s", "");
        if (jwtToken.isEmpty()) {
            log.debug("Token 为空");
            return null;
        }

        // 检查白名单缓存
        if (tokenWhitelistCache != null) {
            Claims cachedClaims = tokenWhitelistCache.getIfPresent(jwtToken);
            if (cachedClaims != null) {
                log.debug("从缓存中获取到 Token 信息");
                return new JwtToken(cachedClaims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString(), jwtToken, cachedClaims);
            }
        }

        // 解析Token获取userId（失败则视为无效Token）
        try {
            Claims claims = JwtUtils.parseJWT(jwtProperties.getUserSecretKey(), jwtToken);
            String userId = claims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString();
            
            // 加入白名单缓存
            if (tokenWhitelistCache != null) {
                tokenWhitelistCache.put(jwtToken, claims);
            }
            
            log.debug("解析Token成功，userId：{}", userId);
            return new JwtToken(userId, jwtToken, claims);
        } catch (DecodingException e) {
            log.error("Token 解码失败（包含非法字符）：{}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.error("Token 校验失败：{}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Token 解析异常", e);
            return null;
        }
    }

    /**
     * 仅已认证用户放行
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
            return true;
        }
        
        try {
            return onAccessDenied(request, response);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 未认证/Token无效时处理
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        // 尝试解析 Token
        String tokenStr = getRequestToken(request);
        if (tokenStr == null) {
            return true; // 没有 Token，放行（可能是匿名访问接口，交给 Shiro 后续处理）
        }

        try {
            // 解析 JWT
            Claims claims = JwtUtils.parseJWT(jwtProperties.getUserSecretKey(), tokenStr);
            String userId = claims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString();
            
            // 构建 Token
            JwtToken token = new JwtToken(userId, tokenStr, claims);
            
            // 提交给 Realm 进行认证
            Subject subject = getSubject(request, response);
            subject.login(token);
            
            // 认证成功后，手动将 UserInfo 放入 ThreadLocal
            SysUser user = (SysUser) subject.getPrincipal();
            if (user != null) {
                BaseContext.setUserInfo(user.getUserInfo());
            }
            
            // 认证成功，检查刷新 Token
            // refreshTokenIfNeed(response, token); // 暂时注释，先调通主流程

            return true; // 认证通过，放行
        } catch (Exception e) {
            log.error("Token 认证失败", e);
            return onLoginFailure(response, e);
        }
    }

    private String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    private boolean onLoginFailure(ServletResponse response, Exception e) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setContentType("application/json;charset=utf-8");
        httpResponse.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "认证失败：" + e.getMessage())));
        return false;
    }

    /**
     * 刷新 Token 机制：如果 Token 即将过期，则生成新 Token 并在 Header 中返回
     */
    private void refreshTokenIfNeed(ServletResponse response, JwtToken jwtToken) {
        Claims claims = jwtToken.getClaims();
        Date expiration = claims.getExpiration();
        
        // 如果过期时间在 20 分钟内，则刷新（这里假设 TTL 较大，可根据实际调整）
        // 或者使用比例：剩余时间 < 20% TTL
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
        long refreshThreshold = 20 * 60 * 1000; // 20 分钟
        
        if (remainingMillis > 0 && remainingMillis < refreshThreshold) {
            log.info("Token 即将过期，执行自动刷新");
            Map<String, Object> newClaims = new HashMap<>(claims);
            String newToken = JwtUtils.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), newClaims);
            
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setHeader(TOKEN_HEADER, TOKEN_PREFIX + " " + newToken);
            httpResponse.setHeader(REFRESH_TOKEN_HEADER, TOKEN_HEADER);
        }
    }

    /**
     * 请求完成后清理 ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
        BaseContext.removeUserInfo();
        super.afterCompletion(request, response, exception);
    }

    /**
     * 统一返回401响应
     */
    private boolean sendUnAuthorizedResponse(ServletResponse response, String msg) throws IOException {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<Object> result = Result.error(401, msg);
        objectMapper.writeValue(httpResponse.getOutputStream(), result);
        return false;
    }
}