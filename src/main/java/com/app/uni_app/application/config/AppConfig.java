package com.app.uni_app.application.config;


import lombok.Data;

@Data
public class AppConfig {
    // MySQL 配置
    private DbConfig datasource = new DbConfig();
    // ES 配置
    private EsConfig elasticsearch = new EsConfig();

    @Data
    public static class DbConfig {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
    }

    @Data
    public static class EsConfig {
        private String uris; // http://localhost:9200
    }
}
