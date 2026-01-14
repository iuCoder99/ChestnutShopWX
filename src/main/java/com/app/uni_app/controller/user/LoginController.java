package com.app.uni_app.controller.user;

import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDTO;
import com.app.uni_app.pojo.dto.UserWechatDTO;
import com.app.uni_app.service.LoginService;
import jakarta.annotation.Resource;
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
    public Result loginByAccount(@RequestBody UserDTO userDTO) {

        return loginService.loginByAccount(userDTO);
    }

    /**
     * 用户使用微信快捷登录
     *
     * @return
     */
    @PostMapping("/login/wechat")
    public Result loginByWechat(@RequestBody UserWechatDTO userWechatDTO) {
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
    public Result createAccount(@RequestParam String username
            , @RequestParam String password
            , @RequestParam String phone) {
        return loginService.createAccount(username, password, phone);
    }


}
