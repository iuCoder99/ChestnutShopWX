package com.app.uni_app.application.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 最终版YML加载器：支持Spring占位符 + 主配置/dev配置字段合并
 */
public class YamlConfigLoader {

    private static final String MAIN_YML = "application.yml";
    private static final String DEV_YML = "application-dev.yml";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^:]+):?([^}]*)\\}");

    public static AppConfig loadConfig() {
        Yaml yaml = new Yaml();
        AppConfig appConfig = new AppConfig();

        // 1. 先加载【主配置】（获取驱动、URL）
        InputStream mainIs = YamlConfigLoader.class.getClassLoader().getResourceAsStream(MAIN_YML);
        Map<String, Object> mainMap = null;
        if (mainIs != null) {
            mainMap = yaml.load(mainIs);
            fillConfig(appConfig, mainMap);
        }

        // 2. 再加载【dev配置】（只覆盖账号、密码，不覆盖驱动/URL）
        InputStream devIs = YamlConfigLoader.class.getClassLoader().getResourceAsStream(DEV_YML);
        if (devIs != null) {
            Map<String, Object> devMap = yaml.load(devIs);
            mergeDevConfig(appConfig, devMap);
        }

        return appConfig;
    }

    /**
     * 填充主配置到实体
     */
    @SuppressWarnings("unchecked")
    private static void fillConfig(AppConfig config, Map<String, Object> map) {
        if (map == null) return;
        Map<String, Object> spring = (Map<String, Object>) map.get("spring");
        if (spring == null) return;

        // 数据库配置
        Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
        if (datasource != null) {
            config.getDatasource().setDriverClassName(resolve(getStr(datasource.get("driver-class-name"))));
            config.getDatasource().setUrl(resolve(getStr(datasource.get("url"))));
            config.getDatasource().setUsername(resolve(getStr(datasource.get("username"))));
            config.getDatasource().setPassword(resolve(getStr(datasource.get("password"))));
        }

        // ES 配置
        Map<String, Object> es = (Map<String, Object>) spring.get("elasticsearch");
        if (es != null) {
            config.getElasticsearch().setUris(resolve(getStr(es.get("uris"))));
        }
    }

    /**
     * 合并dev配置：只覆盖有值的字段（账号/密码），不清空驱动/URL
     */
    @SuppressWarnings("unchecked")
    private static void mergeDevConfig(AppConfig config, Map<String, Object> map) {
        if (map == null) return;
        Map<String, Object> spring = (Map<String, Object>) map.get("spring");
        if (spring == null) return;

        Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
        if (datasource != null) {
            // 只覆盖dev里存在的配置，不覆盖null值（保护驱动、URL）
            if (datasource.containsKey("username")) {
                config.getDatasource().setUsername(resolve(getStr(datasource.get("username"))));
            }
            if (datasource.containsKey("password")) {
                config.getDatasource().setPassword(resolve(getStr(datasource.get("password"))));
            }
        }
    }

    // 解析 Spring 占位符
    private static String resolve(String value) {
        if (value == null) return null;
        Matcher m = PLACEHOLDER_PATTERN.matcher(value);
        if (m.matches()) return m.group(2).isEmpty() ? m.group(1) : m.group(2);
        return value;
    }

    // 安全转字符串
    private static String getStr(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}