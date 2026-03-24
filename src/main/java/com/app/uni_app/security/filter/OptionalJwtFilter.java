package com.app.uni_app.security.filter;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * 可选认证过滤器：
 * 1. 如果请求头中包含 Token，则进行认证（逻辑同 JwtFilter）；
 * 2. 如果不包含 Token，则直接放行（匿名访问）。
 */
public class OptionalJwtFilter extends JwtFilter {

    /**
     * 重写访问控制逻辑
     * @return true: 直接放行; false: 进入 onAccessDenied 认证
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 跨域 OPTIONS 请求直接放行
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
            return true;
        }

        // 检查是否有 Token
        String token = getRequestToken((HttpServletRequest) request);

        // 如果没有 Token，则放行（由业务逻辑处理 userId 为空的情况）
        if (StringUtils.isBlank(token)) {
            return true;
        }

        // 如果有 Token，返回 false 以触发 onAccessDenied 进行登录认证
        return false;
    }
}
