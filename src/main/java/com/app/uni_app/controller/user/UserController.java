package com.app.uni_app.controller.user;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDetailDTO;
import com.app.uni_app.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 获取用户详情
     *
     * @return
     */
    @GetMapping("/detail/get")
    public Result getUserDetail() {
        return userService.getUserDetail();
    }

    @PutMapping("/detail/update")
    public Result updateUserDetail(@RequestBody @NotNull UserDetailDTO userDetailDTO) {
        return userService.updateUserDetail(userDetailDTO);
    }


}
