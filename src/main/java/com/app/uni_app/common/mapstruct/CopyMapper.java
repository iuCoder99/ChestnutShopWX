package com.app.uni_app.common.mapstruct;

import com.app.uni_app.common.result.UserInfo;
import com.app.uni_app.pojo.dto.*;
import com.app.uni_app.pojo.entity.*;
import com.app.uni_app.pojo.vo.ProductSpecVO;
import com.app.uni_app.pojo.vo.UserDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CopyMapper {

    UserInfo userToUserInfo(User user);

    Banner bannerDTOToBanner(BannerDTO bannerDTO);

    Cart cartProductDTOToCart(CartProductDTO cartProductDTO);

    Category categoryDTOToCategory(CategoryDTO categoryDTO);

    Notice noticeDTOToNotice(NoticeDTO noticeDTO);

    ProductSpecVO productSpecToProductSpecVO(ProductSpec productSpec);

    UserDetailVO userToUserDetailVO(User user);
@Mapping(target = "id",expression = "java(addressDTO.getId().isBlank()? null:Long.valueOf(addressDTO.getId()))")
    Address addressDTOToAddress(AddressDTO addressDTO);
}
