package com.app.uni_app.application.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.app.uni_app.infrastructure.es.common.mapstruct.EsCopyMapper;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.index.EsIndexEnum;
import com.app.uni_app.mapper.ProductMapper;
import com.app.uni_app.pojo.entity.Product;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mapstruct.factory.Mappers;
import org.elasticsearch.client.RestClient;

import javax.sql.DataSource;
import java.util.List;

/**
 * 手动同步 MySQL 商品数据到 ElasticSearch
 */
@Slf4j
public class ProductDataSyncApplication {

    // ===================== 配置项 =====================
    private static final String DB_URL = "jdbc:mysql://localhost:3306/uni_app?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PWD = "123456";
    private static final String ES_INDEX = EsIndexEnum.PRODUCT.getIndexName(); // ES 索引名
    private static final String ES_HOST = "localhost";
    private static final int ES_PORT = 9200;

    private static final EsCopyMapper ES_COPY_MAPPER = Mappers.getMapper(EsCopyMapper.class);

    public static void main(String[] args) {
        //  手动初始化 MyBatis-Plus
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();

        // 自动关闭资源（SqlSession + ES客户端）
        try (SqlSession session = sqlSessionFactory.openSession(true);
             ElasticsearchClient esClient = getEsClient()) {

            //  查询 MySQL 全量商品数据
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

            //  批量同步到 ES
            bulkSyncToEs(esClient, productDocumentList);

            System.out.println(" 同步成功！总数据量：" + productList.size());

        } catch (Exception e) {
            log.error(" 数据同步失败", e);
        }
    }

    /**
     * 手动初始化 MyBatis-Plus
     */
    private static SqlSessionFactory getSqlSessionFactory() {
        //  初始化数据库连接池
        DataSource dataSource = new PooledDataSource(
                "com.mysql.cj.jdbc.Driver",
                DB_URL,
                DB_USER,
                DB_PWD
        );

        //  创建事务工厂（MyBatis 原生 JDBC 事务）
        TransactionFactory transactionFactory = new JdbcTransactionFactory();

        //  构建 Environment 环境对象（绑定 事务工厂 + 数据源）
        Environment environment = new Environment(
                "development", // 环境id，自定义即可
                transactionFactory,
                dataSource
        );

        // MyBatis-Plus 配置
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(ProductMapper.class);
        //将 Environment 赋值给配置类（修复空指针的关键！）
        configuration.setEnvironment(environment);

        //  SqlSessionFactory
        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * ES 客户端
     */
    private static ElasticsearchClient getEsClient() {
        RestClient restClient = RestClient.builder(new HttpHost(ES_HOST, ES_PORT, "http")).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
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

        // 执行批量请求
        esClient.bulk(bulkBuilder.build());
    }
}