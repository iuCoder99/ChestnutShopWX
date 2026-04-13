package com.app.uni_app.application.es.init;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.app.uni_app.application.common.EsClientUtil;
import com.app.uni_app.infrastructure.es.index.EsIndexInitializerService.EsIndexInitializerService;
import com.app.uni_app.infrastructure.es.index.EsIndexInitializerService.impl.EsIndexInitializerServiceImpl;
import lombok.extern.slf4j.Slf4j;


/**
 * ES 索引初始化器
 */
@Slf4j
public class EsIndexInitializer {

    public static void main(String[] args) {
        //  复用工具类获取ES客户端（自动读取yml配置）
        //  try-with-resources 自动关闭资源
        try (ElasticsearchClient client = EsClientUtil.getEsClient()) {
            //  执行业务逻辑
            EsIndexInitializerService initializerService = new EsIndexInitializerServiceImpl(client);
            initializerService.initProductIndex();
        } catch (Exception e) {
           log.error("es 索引初始化异常" , e);
        }
    }
}