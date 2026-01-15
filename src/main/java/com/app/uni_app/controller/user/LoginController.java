package com.app.uni_app.controller.user;

import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDTO;
import com.app.uni_app.pojo.dto.UserWechatDTO;
import com.app.uni_app.service.LoginService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class LoginController {
    @Resource
    private LoginService loginService;

    /**
     * 用户使用账户密码登录
     *
     * @param userDTO
     * @return
     */
    @PostMapping("/login/account")
    public Result loginByAccount(@RequestBody @NotNull UserDTO userDTO) {

        return loginService.loginByAccount(userDTO);
    }

    /**
     * 用户使用微信快捷登录
     *
     * @return
     */
    @PostMapping("/login/wechat")
    public Result loginByWechat(@RequestBody @NotNull UserWechatDTO userWechatDTO) {
        return loginService.loginByWechat(userWechatDTO);
    }

    /**
     * 获取登录用户信息
     *
     * @return
     */
    @GetMapping("/info")
    public Result getUser() {
        return loginService.getUser();
    }

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/logout")
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
    public Result changePassword(@RequestParam @NotBlank String username
            , @RequestParam @NotBlank String passwordOld
            , @RequestParam @NotBlank String passwordNew) {
        return loginService.changePassword(username, passwordOld, passwordNew);
    }

}
