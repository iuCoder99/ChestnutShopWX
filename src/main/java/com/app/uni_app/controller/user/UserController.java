package com.app.uni_app.controller.user;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDetailDTO;
import com.app.uni_app.service.CollectionService;
import com.app.uni_app.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
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
    public Result getCollectionList(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return collectionService.getCollectionList(pageNum,pageSize);
    }


}
