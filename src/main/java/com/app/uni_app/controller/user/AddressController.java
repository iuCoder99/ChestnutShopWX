package com.app.uni_app.controller.user;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.AddressDTO;
import com.app.uni_app.service.AddressService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    @Resource
    private AddressService addressService;

    /**
     * 获取地址列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result getAddressList() {
        return addressService.getAddressList();
    }

    /**
     * 新增地址列表
     *
     * @param addressDTO
     * @return
     */
    @PostMapping("/add")
    public Result insertAddress(@RequestBody @Valid AddressDTO addressDTO) {
        return addressService.insertAddress(addressDTO);

    }

    /**
     * 修改地址
     *
     * @param addressDTO
     * @return
     */
    @PutMapping("/update")
    public Result updateAddress(@RequestBody @Valid AddressDTO addressDTO) {
        return addressService.updateAddress(addressDTO);
    }

    /**
     * 删除地址
     *
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public Result deleteAddress(@RequestParam String id) {
        return addressService.deleteAddress(id);

    }
}
