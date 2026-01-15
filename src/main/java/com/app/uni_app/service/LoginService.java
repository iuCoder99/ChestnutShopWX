package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDTO;
import com.app.uni_app.pojo.dto.UserWechatDTO;
import com.app.uni_app.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface LoginService extends IService<User> {
    Result loginByAccount(@NotNull UserDTO userDTO);

    Result loginByWechat(@NotNull UserWechatDTO userWechatDTO);

    Result getUser();

    Result createAccount(@NotBlank String username, @NotBlank String password, @NotBlank String phone);

    Result forgetPassword(@NotBlank String username, @NotBlank String phone,@NotBlank String passwordNew);

    Result changePassword(@NotBlank String username, @NotBlank String passwordOld, @NotBlank String passwordNew);
}


