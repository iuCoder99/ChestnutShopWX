package com.app.uni_app.application.common;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.app.uni_app.application.config.ConfigHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

/**
 * ES 客户端工具类（读取 yml 配置）
 * 可以独立启动 elasticSearch , 无需启动项目
 */
public class EsClientUtil {

    public static ElasticsearchClient getEsClient() {
        // 从 yml 配置读取 ES 地址
        String esHost = ConfigHolder.getEsHost();
        int esPort = ConfigHolder.getEsPort();

        RestClient restClient = RestClient.builder(new HttpHost(esHost, esPort, "http")).build();

        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 关闭时间戳格式，使用标准日期格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 使用自定义的 ObjectMapper 创建 Jackson映射器
        JacksonJsonpMapper mapper = new JacksonJsonpMapper(objectMapper);

        ElasticsearchTransport transport = new RestClientTransport(restClient, mapper);
        return new ElasticsearchClient(transport);
    }
}