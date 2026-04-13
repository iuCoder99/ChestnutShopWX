package com.app.uni_app.infrastructure.es.index.EsIndexInitializerService.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.index.EsIndexEnum;
import com.app.uni_app.infrastructure.es.index.EsIndexInitializerService.EsIndexInitializerService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Function;

/**
 * 初始化 es 索引 , 只为启动类提供服务
 */
@Slf4j
public class EsIndexInitializerServiceImpl implements EsIndexInitializerService {

    private final ElasticsearchClient elasticsearchClient;


    public EsIndexInitializerServiceImpl(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public void initProductIndex() {
        String indexName = EsIndexEnum.PRODUCT.getIndexName();
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(indexName))
                    .value();

            if (exists) {
                log.info("ES索引[{}]已存在，跳过初始化", indexName);
                return;
            }

            Function<CreateIndexRequest.Builder, ObjectBuilder<CreateIndexRequest>> request = builder ->
                    builder.index(indexName)
                            .settings(IndexSettings.of(s -> s.numberOfShards(String.valueOf(1)).numberOfReplicas(String.valueOf(0))))
                            .mappings(m -> m
                                    .properties(ProductDocument.Fields.id, p -> p.long_(l -> l))
                                    .properties(ProductDocument.Fields.categoryId, p -> p.long_(l -> l))

                                    .properties(ProductDocument.Fields.name, p -> p.text(t -> t
                                            .fields("keyword", k -> k.keyword(kw -> kw))
                                    ))

                                    .properties(ProductDocument.Fields.sellPoint, p -> p.text(t -> t))
                                    .properties(ProductDocument.Fields.price, p -> p.scaledFloat(f -> f.scalingFactor(100.0)))
                            );

            elasticsearchClient.indices().create(request);
            log.info("ES索引[{}]初始化成功", indexName);

        } catch (IOException e) {
            log.error("ES索引[{}]初始化失败", indexName, e);
            throw new RuntimeException("商品 ES文档初始化异常", e);
        }
    }
}