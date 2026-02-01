package com.app.uni_app.controller.user;

import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDTO;
import com.app.uni_app.pojo.dto.UserWechatDTO;
import com.app.uni_app.service.SysLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户登录注册管理")
public class LoginController {
    @Resource
    private SysLoginService loginService;

    /**
     * 用户使用账户密码登录
     *
     * @param userDTO
     * @return
     */
    @PostMapping("/login/account")
    @Operation(summary = "账户密码登录", description = "用户通过用户名和密码进行登录")
    public Result loginByAccount(@RequestBody @NotNull UserDTO userDTO) {
        return loginService.loginByAccount(userDTO);
    }

    /**
     * 用户使用微信快捷登录
     *
     */
    @PostMapping("/login/wechat")
    @Operation(summary = "微信快捷登录", description = "用户通过微信进行快捷登录")
    public Result loginByWechat(@RequestBody @NotNull UserWechatDTO userWechatDTO) {
        return loginService.loginByWechat(userWechatDTO);
    }

    /**
     * 获取登录用户信息
     *
     * @return
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的个人信息")
    public Result getUser() {
        return loginService.getUser();
    }

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "用户退出登录，清除当前登录状态")
    public Result logout() {
        BaseContext.removeUserInfo();
        return Result.success();
    }

    /**
     * 创建普通用户账户
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/create/account")
    @Operation(summary = "创建用户账户", description = "创建普通用户账户，需提供用户名、密码和手机号")
    public Result createAccount(@RequestParam @NotBlank String username
            , @RequestParam @NotBlank String password
            , @RequestParam @NotBlank String phone) {
        return loginService.createAccount(username, password, phone);
    }

    /**
     * 忘记密码
     *
     * @param username
     * @param phone
     * @param passwordNew
     * @return
     */
    @PutMapping("/forget/password")
    @Operation(summary = "忘记密码重置", description = "通过用户名和手机号验证，重置新密码")
    public Result forgetPassword(@RequestParam @NotBlank String username
            , @RequestParam @NotBlank String phone
            , @RequestParam @NotBlank String passwordNew) {
        return loginService.forgetPassword(username, phone, passwordNew);
    }

    /**
     * 修改密码
     * @param username
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @PutMapping("/change/password")
    @Operation(summary = "修改密码", description = "验证旧密码后，修改为新密码")
    public Result changePassword(@RequestParam @NotBlank String username
            , @RequestParam @NotBlank String passwordOld
            , @RequestParam @NotBlank String passwordNew) {
        return loginService.changePassword(username, passwordOld, passwordNew);
    }

}