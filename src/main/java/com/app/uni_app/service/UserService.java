package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.UserDetailDTO;
import com.app.uni_app.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    Result getUserDetail();

    Result updateUserDetail(UserDetailDTO userDetailDTO);

}
