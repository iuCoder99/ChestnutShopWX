package com.app.uni_app.application.config;

public class ConfigHolder {
    private static final AppConfig CONFIG;

    static {
        CONFIG = YamlConfigLoader.loadConfig();
    }

    public static AppConfig getConfig() {
        return CONFIG;
    }

    /**
     * 增加空值防护，配置不存在时抛出清晰提示
     */
    public static String getEsHost() {
        String uris = CONFIG.getElasticsearch().getUris();
        if (uris == null || uris.isBlank()) {
            throw new RuntimeException("ES配置读取失败，请检查application.yml中的spring.elasticsearch.uris配置");
        }
        return uris.replace("http://", "").split(":")[0];
    }

    public static int getEsPort() {
        String uris = CONFIG.getElasticsearch().getUris();
        if (uris == null || uris.isBlank()) {
            throw new RuntimeException("ES配置读取失败，请检查application.yml中的spring.elasticsearch.uris配置");
        }
        return Integer.parseInt(uris.replace("http://", "").split(":")[1]);
    }
}