package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.AddressDTO;
import com.app.uni_app.pojo.entity.Address;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

public interface AddressService extends IService<Address> {
    Result getAddressList();

    Result updateAddress(@Valid AddressDTO addressDTO);

    Result insertAddress(@Valid AddressDTO addressDTO);

    Result deleteAddress(String id);
}
