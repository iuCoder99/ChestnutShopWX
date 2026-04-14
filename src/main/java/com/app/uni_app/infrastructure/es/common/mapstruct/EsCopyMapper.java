package com.app.uni_app.infrastructure.es.common.mapstruct;

import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.pojo.vo.SimpleProductVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * es 数据转化类
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE, // 忽略字段不匹配警告
        unmappedSourcePolicy = ReportingPolicy.IGNORE)  // 忽略源对象多余字段)
public interface EsCopyMapper {
    @Mapping(target = "status", source = "status", qualifiedByName = "commonStatusToNumber")
    ProductDocument ProductToProductDocument(Product product);

    @Mapping(target = "status", source = "status", qualifiedByName = "numberToCommonStatusEnum")
    SimpleProductVO ProductDocumentToSimpleProductVO(ProductDocument productDocument);


    /**
     * 枚举数字转通用状态枚举
     * @param number 枚举数字
     * @return 通用状态枚举
     */
    @Named("numberToCommonStatusEnum")
    default CommonStatus numberToCommonStatus(Integer number) {
        for (CommonStatus status : CommonStatus.values()) {
            if (status.getNumber().equals(number)) {
                return status;
            }
        }
        return null;
    }

    @Named("commonStatusToNumber")
    default Integer commonStatusToNumber(CommonStatus status) {
        return status == null ? 0 : status.getNumber();
    }
}
