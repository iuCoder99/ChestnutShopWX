package com.app.uni_app.config;

import com.app.uni_app.common.util.JwtUtils;
import com.app.uni_app.properties.JwtProperties;
import com.app.uni_app.security.filter.JwtFilter;
import com.app.uni_app.security.realm.CustomRealm;
import com.app.uni_app.service.SysLoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.Filter;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    // 1. 自定义Realm（认证+授权核心）
    @Bean
    public CustomRealm customRealm(JwtProperties jwtProperties, SysLoginService sysLoginService) {
        CustomRealm realm = new CustomRealm();
        realm.setJwtProperties(jwtProperties);
        realm.setSysLoginService(sysLoginService);
        return realm;
    }

    // 2. 彻底禁用Session（前后端分离必需）
    @Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionIdCookieEnabled(false); // 关闭Session Cookie
        sessionManager.setSessionIdUrlRewritingEnabled(false); // 关闭 URL 重写
        sessionManager.setGlobalSessionTimeout(-1); // 禁用 Session 超时
        return sessionManager;
    }

    // 3. Shiro核心管理器（禁用Session存储）
    @Bean
    public SecurityManager securityManager(CustomRealm customRealm, DefaultWebSessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(customRealm);
        securityManager.setSessionManager(sessionManager);

        // 强制禁用Session存储（避免依赖Session导致认证混乱）
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        return securityManager;
    }

    // 4. 统一返回格式的ObjectMapper
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    // 5. 自定义JwtFilter
    @Bean
    public JwtFilter jwtFilter(JwtUtils jwtUtils, JwtProperties jwtProperties, ObjectMapper objectMapper) {
        JwtFilter jwtFilter = new JwtFilter();
        jwtFilter.setJwtUtils(jwtUtils);
        jwtFilter.setJwtProperties(jwtProperties);
        jwtFilter.setObjectMapper(objectMapper);
        
        // 配置 Token 白名单缓存，减少解析开销，并处理解析失败时的快速降级
        jwtFilter.setTokenWhitelistCache(Caffeine.newBuilder()
                .expireAfterWrite(java.time.Duration.ofMinutes(10))
                .maximumSize(1000)
                .build());
        
        return jwtFilter;
    }

    /**
     * 禁用 Spring Boot 自动注册 JwtFilter 为全局过滤器
     * 避免 UnavailableSecurityManagerException
     */
    @Bean
    public FilterRegistrationBean<JwtFilter> registration(JwtFilter filter) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    // 6. 核心：Shiro过滤器工厂（无任何javax依赖）
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, JwtFilter jwtFilter) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);

        // 注册JwtFilter（Jakarta的Filter，Shiro 1.12完全兼容）
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("jwt", jwtFilter);
        factoryBean.setFilters(filters);

        // 拦截规则（顺序：自上而下，公开接口在前）
        Map<String, String> filterChainDefinitionMap = getStringStringMap();

        factoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return factoryBean;
    }

    private static @NonNull Map<String, String> getStringStringMap() {
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 公开接口（无需认证）
        filterChainDefinitionMap.put("/api/user/login/**", "anon");
        filterChainDefinitionMap.put("/api/user/create/account", "anon");
        filterChainDefinitionMap.put("/api/user/forget/password", "anon");
        filterChainDefinitionMap.put("/api/user/change/password", "anon");
        filterChainDefinitionMap.put("/api/banner/list", "anon");
        filterChainDefinitionMap.put("/api/product/**", "anon");
        filterChainDefinitionMap.put("/api/category/**", "anon");
        filterChainDefinitionMap.put("/api/notice/**", "anon");


        filterChainDefinitionMap.put("/swagger-ui/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");


        // 静态资源
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        // 角色校验接口（需认证+对应角色）
     //   filterChainDefinitionMap.put("/api/admin/**", "jwt,roles[admin]");
     //   filterChainDefinitionMap.put("/api/user/**", "jwt,roles[user]");
        // 其他所有接口必须认证
        filterChainDefinitionMap.put("/**", "jwt");
        return filterChainDefinitionMap;
    }

    // 7. 启用Shiro注解支持（@RequiresRoles等）
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    // 8. AOP自动代理（确保注解生效）
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true); // 强制 cglib代理
        return creator;
    }

    // 9. Shiro生命周期处理器
    @Bean
    public static org.apache.shiro.spring.LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new org.apache.shiro.spring.LifecycleBeanPostProcessor();
    }
}