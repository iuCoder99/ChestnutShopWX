package com.app.uni_app.infrastructure.es.index;

import com.app.uni_app.pojo.entity.Product;
import lombok.Getter;

@Getter
public enum EsIndexEnum {

    PRODUCT("product_index", Product.class ,"商品数据索引");

    /**
     * 索引名称
     */
    private final String indexName;

    /**
     * 对应数据库实体类
     */
    private final Class<?> entityClass;

    /**
     * 索引描述
     */
    private final String desc;


    EsIndexEnum(String indexName, Class<?> entityClass,String desc) {
        this.indexName = indexName;
        this.entityClass = entityClass;
        this.desc = desc;
    }


}
