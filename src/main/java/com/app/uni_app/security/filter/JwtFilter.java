package com.app.uni_app.security.filter;

import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.util.JwtUtils;
import com.app.uni_app.pojo.entity.SysUser;
import com.app.uni_app.properties.JwtProperties;
import com.app.uni_app.security.token.JwtToken;
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
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Setter
public class JwtFilter extends AuthenticatingFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);


    // 仅用setter注入（ShiroConfig中配置，避免@Resource冲突）
    private JwtProperties jwtProperties;
    private Cache<String, Claims> tokenWhitelistCache;
    private HandlerExceptionResolver handlerExceptionResolver;

    /**
     * 1
     * 预处理：跨域OPTIONS请求放行
     * 流程：
     * 1. 转换 request/response 为 HttpServletRequest/HttpServletResponse
     * 2. 设置跨域响应头（允许 Origin, Methods, Headers, Credentials）
     * 3. 如果是 OPTIONS 请求，直接返回 200 OK，不继续执行后续 Filter
     * 4. 如果是其他请求，继续执行 super.preHandle() -> 进入 isAccessAllowed()
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        // 跨域配置（宽松适配前端）
        httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", jwtProperties.getUserTokenName() + ",Content-Type");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * (未使用)
     * 核心：解析Token，封装JwtToken（确保userId非空）
     * 被调用时机：Subject.login() 方法内部调用
     * 注意：
     * 1. 此时 request 中应该已经包含 Token
     * 2. 如果 Token 格式错误/为空，直接返回 null -> login 失败
     * 3. 如果 Token 解析成功，返回 JwtToken 对象（包含 userId, token, claims）
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(servletRequest);
        String token = getRequestToken(httpRequest);

        // 检查白名单缓存
        if (tokenWhitelistCache != null) {
            Claims cachedClaims = tokenWhitelistCache.getIfPresent(token);
            if (cachedClaims != null) {
                log.debug("从缓存中获取到 Token 信息");
                return new JwtToken(cachedClaims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString(), token, cachedClaims);
            }
        }

        // 解析Token获取userId（失败则视为无效Token）
        try {
            Claims claims = JwtUtils.parseJWT(jwtProperties.getUserSecretKey(), token);
            String userId = claims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString();

            // 加入白名单缓存
            if (tokenWhitelistCache != null) {
                tokenWhitelistCache.put(token, claims);
            }

            log.debug("解析Token成功，userId：{}", userId);
            return new JwtToken(userId, token, claims);
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
     * 2
     * 访问控制核心方法
     * 流程：
     * 1. 拦截所有请求（除了 OPTIONS）
     * 2. 因为是无状态 API，我们不检查 Session，直接返回 false，强制进入 onAccessDenied 进行 Token 认证
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
            return true;
        }
        return false;
    }

    /**
     * 3
     * 认证失败处理（核心认证流程）
     * 流程：
     * 1. 尝试从 Header 获取 Token
     * 2. 无 Token -> 抛出未登录异常
     * 3. 有 Token -> 解析 Token -> 成功/token过期异常/token伪造异常 (以上异常均由jwt异常自定义捕捉器捕捉)
     * 4. 构建 JwtToken 对象 -> 调用 subject.login(token)
     * 5. login 成功 -> 存储 UserInfo 到 ThreadLocal -> 返回 true (放行)
     * 6. login 失败/Token 异常 -> 捕获异常 -> 委托 HandlerExceptionResolver 处理 -> 返回 false (拦截)
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            // 尝试解析 Token
            String tokenStr = getRequestToken(request);
            if (tokenStr == null) {
                throw new UnauthenticatedException(MessageConstant.NO_ACCESS_TOKEN);
            }

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

            return true; // 认证通过，放行
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
            return false;
        }
    }

    private String getRequestToken(HttpServletRequest request) {
       return request.getHeader(jwtProperties.getUserTokenName());

    }

    /**
     * 请求结束后的清理工作
     * 作用：
     * 1. 无论请求成功还是失败，只要 Filter 执行过，最后都会执行此方法
     * 2. 关键任务：清理 ThreadLocal 中的 UserInfo，防止内存泄漏
     */
    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
        BaseContext.removeUserInfo();
        super.afterCompletion(request, response, exception);
    }


}