package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDTO;
import com.app.uni_app.pojo.dto.UserWechatDTO;
import com.app.uni_app.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LoginService extends IService<User> {
    Result loginByAccount(UserDTO userDTO);

    Result loginByWechat(UserWechatDTO userWechatDTO);

    Result getUser();

    Result createAccount(String username, String password, String phone);
}

