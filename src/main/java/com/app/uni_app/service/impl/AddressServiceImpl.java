package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.mapper.AddressMapper;
import com.app.uni_app.pojo.dto.AddressDTO;
import com.app.uni_app.pojo.emums.CommonDefault;
import com.app.uni_app.pojo.entity.Address;
import com.app.uni_app.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Resource
    private CopyMapper copyMapper;

    @Resource
    private AddressServiceImpl addressServiceProxy;


    private static final String ADDRESS_ID = "addressId";
    private static final String DELETED = "deleted";

    /**
     * 获取地址列表
     *
     * @return
     */
    @Override
    public Result getAddressList() {
        String userId = BaseContext.getUserInfo().getId();
        List<Address> list = lambdaQuery().eq(Address::getUserId, userId).list();
        return Result.success(list);
    }

    /**
     * 新增地址列表
     *
     * @param addressDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result insertAddress(AddressDTO addressDTO) {
        String userId = BaseContext.getUserInfo().getId();
        Address address = copyMapper.addressDTOToAddress(addressDTO);
        address.setUserId(Long.valueOf(userId));
        addressServiceProxy.makeOnlyHaveOneDefault(addressDTO, userId);
        boolean isSuccess = save(address);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(address);
    }

    /**
     * 保证只有一个默认地址
     * @param addressDTO
     * @param userId
     */
    @Transactional(rollbackFor = Exception.class)
    void makeOnlyHaveOneDefault(AddressDTO addressDTO, String userId) {
        boolean isHaveDefaultInList = false;
        long haveDefaultId = -1;
        if (addressDTO.getIsDefault().getIsDefault()) {
            List<Address> addressList = lambdaQuery().eq(Address::getUserId, userId).list();
            if (CollectionUtils.isNotEmpty(addressList)) {
                for (Address addressInTheList : addressList) {
                    if (addressInTheList.getIsDefault().getIsDefault()) {
                        isHaveDefaultInList = true;
                        haveDefaultId = addressInTheList.getId();
                    }
                }

            }
        }
        if (isHaveDefaultInList) {
            lambdaUpdate().eq(Address::getUserId, userId).eq(Address::getId, haveDefaultId)
                    .set(Address::getIsDefault, CommonDefault.NO_DEFAULT.getNumber()).update();
        }
    }

    /**
     * 修改地址
     *
     * @param addressDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result updateAddress(AddressDTO addressDTO) {
        String userId = BaseContext.getUserInfo().getId();
        Address address = copyMapper.addressDTOToAddress(addressDTO);
        addressServiceProxy.makeOnlyHaveOneDefault(addressDTO, userId);
        address.setUserId(Long.valueOf(userId));
        boolean isSuccess = updateById(address);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(address);
    }

    /**
     * 删除地址
     *
     * @param id
     * @return
     */
    @Override
    public Result deleteAddress(String id) {
        boolean isSuccess = removeById(id);
        if (!isSuccess) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(ADDRESS_ID, id);
        map.put(DELETED, true);
        return Result.success(map);
    }
}
