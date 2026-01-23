package com.app.uni_app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Swagger全局配置（仅在dev/test环境生效，生产环境禁用）
 * 适配SpringDoc OpenAPI 2.2.0（Spring Boot 3.x + JDK17）
 */
@Configuration
@Profile({"dev", "test"}) // 生产环境自动禁用，符合安全规范
public class SwaggerConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeEnv;

    @Bean
    public OpenAPI customOpenAPI() {
        // 1. 环境区分配置
        Server devServer = new Server().url("http://localhost:8080").description("开发环境");
        Server testServer = new Server().url("http://test-api.uni-app.com").description("测试环境");
        List<Server> servers = activeEnv.equals("dev") ? List.of(devServer) : List.of(testServer);

        // 2. 全局响应码配置（SpringDoc 2.2.0 原生支持 description 链式调用）
        // 无需设置 responseCode！addResponses 的 key（如"200"）就是响应码标识
        ApiResponse success = new ApiResponse().description("请求成功（业务逻辑执行成功）");
        ApiResponse badRequest = new ApiResponse().description("参数错误（格式/校验/规则失败）");
        ApiResponse unauthorized = new ApiResponse().description("未授权（登录态失效/未登录）");
        ApiResponse forbidden = new ApiResponse().description("权限不足（无接口访问权限）");
        ApiResponse notFound = new ApiResponse().description("资源不存在（目标数据未找到）");
        ApiResponse serverError = new ApiResponse().description("服务器内部错误（非业务异常）");

        // 3. 全局组件（响应码 + JWT认证）
        var components = new io.swagger.v3.oas.models.Components()
                // key 与响应码一一对应，Swagger文档自动显示 key 作为响应码
                .addResponses("200", success)
                .addResponses("400", badRequest)
                .addResponses("401", unauthorized)
                .addResponses("403", forbidden)
                .addResponses("404", notFound)
                .addResponses("500", serverError)
                // JWT认证配置（全局生效，登录/注册接口可通过 @Operation(security = {}) 关闭）
                .addSecuritySchemes("JWT", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT令牌：登录后从响应中获取，格式为 Bearer {token}"));

        // 4. 构建OpenAPI文档
        return new OpenAPI()
                .servers(servers)
                .info(new Info()
                        .title("uni_app 小程序后端API文档")
                        .version("1.0.0")
                        .description("""
                                1. 文档范围：用户管理、订单管理、商品管理核心接口；
                                2. 认证方式：JWT令牌（登录接口除外）；
                                3. 响应格式：统一返回 {code: 响应码, msg: 提示信息, data: 业务数据}；
                                4. 敏感信息：手机号/密码等字段已脱敏展示，禁止用于生产环境。
                                """)
                        .contact(new Contact().name("后端研发组").email("dev@uni-app.com"))
                        .license(new License().name("内部使用").url("http://uni-app.com/license")))
                .components(components)
                // 全局启用JWT认证
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }
}