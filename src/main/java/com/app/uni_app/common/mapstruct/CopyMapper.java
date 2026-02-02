package com.app.uni_app.common.mapstruct;

import com.app.uni_app.common.result.UserInfo;
import com.app.uni_app.pojo.dto.*;
import com.app.uni_app.pojo.entity.*;
import com.app.uni_app.pojo.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE, // 忽略字段不匹配警告
        unmappedSourcePolicy = ReportingPolicy.IGNORE)  // 忽略源对象多余字段)
public interface CopyMapper {

    UserInfo sysUserToUserInfo(SysUser sysUser);

    Banner bannerDTOToBanner(BannerDTO bannerDTO);

    Cart cartProductDTOToCart(CartProductDTO cartProductDTO);

    Category categoryDTOToCategory(CategoryDTO categoryDTO);

    Notice noticeDTOToNotice(NoticeDTO noticeDTO);

    ProductSpecVO productSpecToProductSpecVO(ProductSpec productSpec);

    UserDetailVO sysUserToUserDetailVO(SysUser sysUser);

    @Mapping(target = "id", expression = "java(addressDTO.getId().isBlank()? null:Long.valueOf(addressDTO.getId()))")
    Address addressDTOToAddress(AddressDTO addressDTO);

    @Mapping(source = "id", target = "id")
    SimpleProductVO productToSimpleProductVO(Product product);

    Order orderDTOToOrder(OrderDTO orderDTO);

    OrderItem orderItemDTOToOrderItem(OrderItemDTO orderItemDTO);

    OrderWithItemVO orderToOrderWithItemVO(Order order);

    OrderAddressVO addressToOrderAddressVO(Address address);

    OrderItemVO orderItemToOrderItemVO(OrderItem orderItem);

    @Mapping(source = "id", target = "orderId")
    OrderWithTrackingVO orderToOrderWithTrackingVO(Order order);

    OrderTrackingVO orderTrackingToOrderTrackingVO(OrderTracking orderTracking);

    FactoryInfoVO factoryInfoToFactoryInfoVO(FactoryInfo factoryInfo);
}
