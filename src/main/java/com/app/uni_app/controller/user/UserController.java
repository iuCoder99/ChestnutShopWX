package com.app.uni_app.controller.user;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDetailDTO;
import com.app.uni_app.service.CollectionService;
import com.app.uni_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户详情与收藏管理")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private CollectionService collectionService;

    /**
     * 获取用户详情
     *
     * @return
     */
    @GetMapping("/detail/get")
    @Operation(summary = "查询用户详情", description = "获取当前登录用户的详细信息")
    public Result getUserDetail() {
        return userService.getUserDetail();
    }

    /**
     * 更改用户详情
     *
     * @param userDetailDTO
     * @return
     */
    @PutMapping("/detail/update")
    @Operation(summary = "修改用户详情", description = "更新当前登录用户的详细信息")
    public Result updateUserDetail(@RequestBody @NotNull UserDetailDTO userDetailDTO) {
        return userService.updateUserDetail(userDetailDTO);
    }

    /**
     * 新增收藏
     *
     * @param productId
     * @return
     */
    @PostMapping("/collect/add")
    @Operation(summary = "新增商品收藏", description = "收藏指定商品（通过商品ID）")
    public Result addCollection(@RequestParam @NotBlank String productId) {
        return collectionService.addCollection(productId);
    }

    /**
     * 删除收藏
     *
     * @param productIds
     * @return
     */
    @DeleteMapping("/collect/delete")
    @Operation(summary = "删除商品收藏", description = "支持单个或批量删除收藏的商品（商品ID以逗号分隔）")
    public Result deleteCollection(@RequestParam String productIds) {
        return collectionService.deleteCollection(productIds);
    }

    /**
     * 获取收藏列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/collect/list")
    @Operation(summary = "查询收藏列表", description = "分页获取当前登录用户的商品收藏列表，默认页码1、每页10条")
    public Result getCollectionList(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return collectionService.getCollectionList(pageNum, pageSize);
    }


}