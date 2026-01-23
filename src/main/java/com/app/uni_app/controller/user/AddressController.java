package com.app.uni_app.controller.user;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.AddressDTO;
import com.app.uni_app.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
@Tag(name = "地址管理")
public class AddressController {
    @Resource
    private AddressService addressService;

    /**
     * 查询地址列表
     *
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "查询地址列表", description = "用户查看自己的地址列表")
    public Result getAddressList() {
        return addressService.getAddressList();

    }

    /**
     * 新增地址
     * @param addressDTO
     * @return
     */
    @PostMapping("/add")
    @Operation(summary = "新增地址", description = "用户新增地址")
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
    @Operation(summary = "修改地址",description = "用户修改地址")
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
    @Operation(summary = "删除地址",description = "用户删除地址")
    public Result deleteAddress(@RequestParam String id) {
        return addressService.deleteAddress(id);

    }
}
