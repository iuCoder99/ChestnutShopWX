package com.app.uni_app.infrastructure.es.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.index.EsIndexEnum;
import com.app.uni_app.infrastructure.es.repository.ProductEsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEsRepositoryImpl implements ProductEsRepository {


    private final ElasticsearchClient esClient;

    /**
     * ES 索引名称
     */
    private static final String PRODUCT_INDEX = EsIndexEnum.PRODUCT.getIndexName();


    @Override
    public ProductDocument getById(Long id) {
        try {
            GetResponse<ProductDocument> response = esClient.get(g -> g
                            .index(PRODUCT_INDEX)
                            .id(id.toString()),
                    ProductDocument.class
            );
            return response.source();
        } catch (IOException e) {
            throw new RuntimeException("ES 查询ID{"+id+"}的商品数据,查询失败", e);
        }
    }

    @Override
    public List<ProductDocument> getById(Long id, Long... ids) {
        List<Long> idList = new ArrayList<>(Arrays.asList(ids));
        idList.add(id);
        return getByIdList(idList);
    }


    @Override
    public List<ProductDocument> getByIdList(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }

        try {
            // 构建 ID 查询
            Query query = Query.of(q -> q.ids(i -> i.values(idList.stream().map(String::valueOf).collect(Collectors.toList()))));

            SearchResponse<ProductDocument> response = esClient.search(s -> s
                            .index(PRODUCT_INDEX)
                            .query(query),
                    ProductDocument.class
            );

            // 提取结果
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("ES 批量根据ID查询失败", e);
        }
    }

    @Override
    public void save(ProductDocument document) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("商品文档或 ID不能为空");
        }
        try {
            esClient.index(i -> i
                    .index(PRODUCT_INDEX)
                    .id(document.getId().toString())
                    .document(document)
            );
        } catch (IOException e) {
            throw new RuntimeException("ES保存商品失败，ID: " + document.getId(), e);
        }
    }

    @Override
    public void batchSave(List<ProductDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
            for (ProductDocument doc : documents) {
                if (doc == null || doc.getId() == null) {
                    throw new IllegalArgumentException("商品文档 或 ID不能为空");
                }
                bulkBuilder.operations(op -> op
                        .index(idx -> idx
                                .index(PRODUCT_INDEX)
                                .id(doc.getId().toString())
                                .document(doc)
                        )
                );
            }
            esClient.bulk(bulkBuilder.build());
        } catch (IOException e) {
            throw new RuntimeException("ES 批量保存商品失败", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("商品 ID不能为空");
        }
        try {
            esClient.delete(d -> d
                    .index(PRODUCT_INDEX)
                    .id(id.toString())
            );
        } catch (IOException e) {
            throw new RuntimeException("ES删除商品失败，ID: " + id, e);
        }
    }

    @Override
    public List<ProductDocument> searchByName(String name) {
        if (!StringUtils.hasText(name)) {
            return List.of();
        }
        try {
            Query query = Query.of(q -> q.match(m -> m
                    .field("name")
                    .query(name)
            ));

            SearchResponse<ProductDocument> response = esClient.search(s -> s
                            .index(PRODUCT_INDEX)
                            .query(query),
                    ProductDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("ES 按名称搜索商品失败", e);
        }
    }
}