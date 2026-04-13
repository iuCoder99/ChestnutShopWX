package com.app.uni_app.infrastructure.es.common.mapstruct;

import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.pojo.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * es 数据转化类
 */
@Mapper( componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE, // 忽略字段不匹配警告
        unmappedSourcePolicy = ReportingPolicy.IGNORE)  // 忽略源对象多余字段)
public interface EsCopyMapper {
    ProductDocument ProductToProductDocument(Product product);
}
