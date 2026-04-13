package com.app.uni_app.application.es.sync;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import com.app.uni_app.application.common.DbUtil;
import com.app.uni_app.application.common.EsClientUtil;
import com.app.uni_app.infrastructure.es.common.mapstruct.EsCopyMapper;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.index.EsIndexEnum;
import com.app.uni_app.mapper.ProductMapper;
import com.app.uni_app.pojo.entity.Product;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 手动同步 MySQL 商品数据到 ElasticSearch
 * 复用：DbUtil（数据库） + EsClientUtil（ES） + ConfigHolder（yml配置）
 */
@Slf4j
public class ProductDataSyncApplication {

    private static final String ES_INDEX = EsIndexEnum.PRODUCT.getIndexName();
    private static final EsCopyMapper ES_COPY_MAPPER = Mappers.getMapper(EsCopyMapper.class);

    public static void main(String[] args) {
        //获取数据库连接（自动读取yml配置）
        SqlSessionFactory sqlSessionFactory = DbUtil.getSqlSessionFactory();

        // 获取ES客户端（自动读取yml配置）
        // try-with-resources 自动关闭资源
        try (SqlSession session = sqlSessionFactory.openSession(true);
             ElasticsearchClient esClient = EsClientUtil.getEsClient()) {

            //  核心业务逻辑
            ProductMapper productMapper = session.getMapper(ProductMapper.class);
            List<Product> productList = productMapper.selectList(
                    Wrappers.lambdaQuery(Product.class).orderByAsc(Product::getId)
            );

            if (productList.isEmpty()) {
                System.out.println("MySQL中无商品数据，同步终止");
                return;
            }

            List<ProductDocument> productDocumentList = productList.stream()
                    .map(ES_COPY_MAPPER::ProductToProductDocument)
                    .toList();

            bulkSyncToEs(esClient, productDocumentList);
            System.out.println("同步成功！总数据量：" + productList.size());

        } catch (Exception e) {
            log.error("数据同步失败", e);
        }
    }

    /**
     * ES 批量同步
     */
    private static void bulkSyncToEs(ElasticsearchClient esClient, List<ProductDocument> esList) throws Exception {
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        for (ProductDocument productDocument : esList) {
            bulkBuilder.operations(op -> op.index(idx ->
                    idx.index(ES_INDEX)
                            .id(productDocument.getId().toString())
                            .document(productDocument)
            ));
        }
        esClient.bulk(bulkBuilder.build());
    }
}