package com.app.uni_app.application.common;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.app.uni_app.application.config.ConfigHolder;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

/**
 * ES 客户端工具类（读取 yml 配置）
 */
public class EsClientUtil {

    public static ElasticsearchClient getEsClient() {
        // 从 yml 配置读取 ES 地址
        String esHost = ConfigHolder.getEsHost();
        int esPort = ConfigHolder.getEsPort();

        RestClient restClient = RestClient.builder(new HttpHost(esHost, esPort, "http")).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}