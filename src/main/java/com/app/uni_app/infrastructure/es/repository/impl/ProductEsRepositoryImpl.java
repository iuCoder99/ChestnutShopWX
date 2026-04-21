package com.app.uni_app.infrastructure.es.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.app.uni_app.aop.annotation.common.ParamCheckAnnotation;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.index.EsIndexEnum;
import com.app.uni_app.infrastructure.es.repository.ProductEsRepository;
import com.app.uni_app.pojo.emums.CommonSortTypeEnum;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.emums.ProductSortTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEsRepositoryImpl implements ProductEsRepository {

    private final ElasticsearchClient esClient;
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
            throw new RuntimeException("ES 查询ID{" + id + "}的商品数据,查询失败", e);
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
            Query query = Query.of(q -> q.ids(i -> i.values(idList.stream().map(String::valueOf).collect(Collectors.toList()))));
            SearchResponse<ProductDocument> response = esClient.search(s -> s
                            .index(PRODUCT_INDEX)
                            .query(query),
                    ProductDocument.class
            );
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("ES 批量根据ID查询失败", e);
        }
    }

    @Override
    public Long getMaxId() {
        try {
            var response = esClient.search(s -> s
                            .index(EsIndexEnum.PRODUCT.getIndexName())
                            .query(q -> q.matchAll(m -> m))
                            .sort(sort -> sort.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Desc)))
                            .size(1)
                            .source(src -> src.filter(f -> f.includes(ProductDocument.Fields.id)))
                    ,ProductDocument.class);

            if (response.hits().total() != null && response.hits().total().value() == 0) {
                throw new RuntimeException("es 商品数据数量为 0 ,未初始化数据...");
            }
            ProductDocument maxIdProductDocument = response.hits().hits().get(0).source();
            if (Objects.isNull(maxIdProductDocument)){
            throw new RuntimeException("es 最大id 商品文档数据异常 ");
            }
            return maxIdProductDocument.getId();
        } catch (IOException e) {
            throw new RuntimeException("查询ES最大ID失败", e);
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
        if (StringUtils.isBlank(name)) {
            return List.of();
        }
        try {
            Query query = Query.of(q -> q.bool(b -> b
                    .must(m -> m.match(ma -> ma.field(ProductDocument.Fields.name)
                            .query(name)))
                    .filter(f -> f.term(t -> t
                            .field(ProductDocument.Fields.status)
                            .value(1)
                    ))
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

    @Override
    public List<ProductDocument> searchByName(String name, Integer limit) {
        if (StringUtils.isBlank(name)) {
            return List.of();
        }
        if (limit == null || limit <= 0) {
            throw new RuntimeException("es 根据名称搜索商品 , limit参数不合法: " + limit);
        }
        try {
            SearchResponse<ProductDocument> response = esClient.search(s -> s
                            .index(PRODUCT_INDEX)
                            .size(limit)
                            .query(q -> q.bool(b -> b
                                    .must(m -> m.match(ma -> ma.field(ProductDocument.Fields.name)
                                            .query(name)))
                                    .filter(f -> f.term(t -> t
                                            .field(ProductDocument.Fields.status)
                                            .value(1)
                                    ))
                            )),
                    ProductDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("ES 按名称搜索商品失败", e);
        }
    }


    @Override
    public List<ProductDocument> searchLimitAfterId(Integer limit, Long productId) {
        try {
            SearchResponse<ProductDocument> response = esClient.search(s -> s
                            .index(EsIndexEnum.PRODUCT.getIndexName())
                            .size(limit)
                            .trackTotalHits(t -> t.enabled(false))
                            .query(q -> q.bool(b -> b
                                    .must(m -> m.range(r -> r
                                            .number(n->n.field(ProductDocument.Fields.id)
                                                    .gt(Double.valueOf(productId))
                                            )
                                    ))
                                    .must(m -> m.term(t -> t
                                            .field(ProductDocument.Fields.status)
                                            .value(CommonStatus.ACTIVE.getNumber())
                                    ))
                            ))
                            .sort(so -> so
                                    .field(f -> f
                                            .field(ProductDocument.Fields.id)
                                            .order(SortOrder.Asc)
                                    )
                            )
                    ,
                    ProductDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("ES商品查询异常", e);
        }
    }

    @Override
    @ParamCheckAnnotation
    public List<ProductDocument> searchLimitByProductSortTypeAndCategoryId(ProductSortTypeEnum productSortTypeEnum, Long categoryId, Integer limit) {
        String sortField = productSortTypeEnum.getSortField();
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;
        SearchRequest.Builder request = new SearchRequest.Builder();
        SearchRequest searchRequest = request.index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field(ProductDocument.Fields.categoryId).value(categoryId)))
                        .filter(f -> f.term(t -> t.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber())))
                ))
                .size(limit)
                .build();
        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 首次游标 limit 查询失败");
        }

    }

    @Override
    public List<ProductDocument> searchCursorByProductSortTypeAndCategoryId(ProductSortTypeEnum productSortTypeEnum, Long categoryId, Integer limit, String sortValue, Long productId) {
        String sortField = productSortTypeEnum.getSortField();
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;
        SearchRequest.Builder request = new SearchRequest.Builder();
        SearchRequest searchRequest = request.index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field(ProductDocument.Fields.categoryId).value(categoryId)))
                        .filter(f -> f.term(t -> t.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber())))
                ))
                .searchAfter(Arrays.asList(
                        FieldValue.of(sortValue),
                        FieldValue.of(productId)
                ))
                .size(limit)
                .build();
        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 游标 limit 查询失败");
        }
    }

    @Override
    @ParamCheckAnnotation
    public List<ProductDocument> searchLimitByProductSortTypeAndCategoryIdList(
            ProductSortTypeEnum productSortTypeEnum,
            List<Long> categoryIdList,
            Integer limit
    ) {
        String sortField = productSortTypeEnum.getSortField();
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .must(m -> m.terms(t -> t
                                .field(ProductDocument.Fields.categoryId)
                                .terms(f -> f.value(
                                        categoryIdList.stream()
                                                .map(FieldValue::of)
                                                .toList()
                                ))
                        ))
                        .must(m -> m.term(t -> t
                                .field(ProductDocument.Fields.status)
                                .value(CommonStatus.ACTIVE.getNumber())
                        ))
                ))
                .size(limit)
                .build();

        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 首次游标 limit 查询失败");
        }
    }

    @Override
    @ParamCheckAnnotation
    public List<ProductDocument> searchCursorByProductSortTypeAndCategoryIdList(
            ProductSortTypeEnum productSortTypeEnum,
            List<Long> categoryIdList,
            Integer limit,
            String sortValue,
            Long productId
    ) {
        String sortField = productSortTypeEnum.getSortField();
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .must(m -> m.terms(t -> t
                                .field(ProductDocument.Fields.categoryId)
                                .terms(f -> f.value(
                                        categoryIdList.stream()
                                                .map(FieldValue::of)
                                                .toList()
                                ))
                        ))
                        .must(m -> m.term(t -> t
                                .field(ProductDocument.Fields.status)
                                .value(CommonStatus.ACTIVE.getNumber())
                        ))
                ))
                .searchAfter(Arrays.asList(
                        FieldValue.of(sortValue),
                        FieldValue.of(productId)
                ))
                .size(limit)
                .build();

        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 游标 limit 查询失败");
        }
    }


    @Override
    public List<ProductDocument> searchLimitByProductSortTypeAndProductName(ProductSortTypeEnum productSortTypeEnum, String keyword, Integer limit) {
        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;
        SearchRequest.Builder request = new SearchRequest.Builder();
        SearchRequest searchRequest = request.index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(productSortTypeEnum.getSortField()).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .must(m -> m.match(ma -> ma
                                .field(ProductDocument.Fields.name)
                                .query(keyword)
                                .fuzziness("AUTO")
                        ))
                        .must(m -> m.term(t -> t.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber())))
                ))
                .size(limit)
                .build();
        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 游标 limit 查询失败");
        }
    }
    @Override
    public List<ProductDocument> searchCursorByProductSortTypeAndProductName(ProductSortTypeEnum productSortTypeEnum, String keyword, Integer limit, String sortValue, Long productId) {
        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;
        SearchRequest.Builder request = new SearchRequest.Builder();
        SearchRequest searchRequest = request.index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(productSortTypeEnum.getSortField()).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .must(m ->m.match(ma->ma.field(ProductDocument.Fields.name).query(keyword).fuzziness("AUTO")))
                        .must(m -> m.term(t -> t.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber())))

                ))
                .searchAfter(Arrays.asList(
                        FieldValue.of(sortValue),
                        FieldValue.of(productId)
                ))
                .size(limit)
                .build();
        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 游标 limit 查询失败");
        }
    }


    @Override
    public List<ProductDocument> searchLimitOrderByField(Integer limit, String fieldName, CommonSortTypeEnum commonSortTypeEnum) {
        if (StringUtils.isBlank(fieldName) || Objects.isNull(limit) || Objects.isNull(commonSortTypeEnum)) {
            return Collections.emptyList();
        }
        try {

            SortOrder order = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;
            SearchResponse<ProductDocument> searchResponse = esClient.search(s -> s.query(q -> q.term(te -> te.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber()
                            )))
                            .sort(so -> so.field(f -> f.field(fieldName).order(order)))
                            .size(limit)
                    , ProductDocument.class);
            return searchResponse.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
        }catch (IOException e){
            throw new RuntimeException("es 字段排序查询失败 ");
        }

    }
}