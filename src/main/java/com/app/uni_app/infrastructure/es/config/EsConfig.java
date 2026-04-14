package com.app.uni_app.infrastructure.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class EsConfig {

    private final EsProperties esProperties;



    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 从 Spring 配置文件读取地址，自动解析 host 和 port
        String uris =esProperties.getUris();
        String address = uris.replace("http://", "");
        String host = address.split(":")[0];
        int port = Integer.parseInt(address.split(":")[1]);

        // 构建 RestClient
        RestClient restClient = RestClient.builder(new HttpHost(host, port, "http")).build();

        //  LocalDateTime 序列化支持
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(objectMapper)
        );

        return new ElasticsearchClient(transport);
    }
}
