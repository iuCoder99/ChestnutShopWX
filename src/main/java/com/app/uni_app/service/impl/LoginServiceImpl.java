package com.app.uni_app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONObject;
import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.LoginInfo;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.result.UserInfo;
import com.app.uni_app.common.utils.JwtUtil;
import com.app.uni_app.common.utils.WechatLoginUtil;
import com.app.uni_app.mapper.LoginMapper;
import com.app.uni_app.pojo.dto.UserDTO;
import com.app.uni_app.pojo.dto.UserWechatDTO;
import com.app.uni_app.pojo.emums.UserType;
import com.app.uni_app.pojo.entity.User;
import com.app.uni_app.properties.JwtProperties;
import com.app.uni_app.service.LoginService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl extends ServiceImpl<LoginMapper, User> implements LoginService {

    @Resource
    private LoginMapper loginMapper;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private WechatLoginUtil wechatLoginUtil;

    /**
     * 用户登录
     * 二级登录,用户自选(可自由切换账号)
     *
     * @param userDTO
     * @return
     */
    @Override
    public Result loginByAccount(UserDTO userDTO) {
        String password = userDTO.getPassword();
        User user = lambdaQuery().eq(User::getUsername, userDTO.getUsername()).one();
        if (user == null) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            return Result.error(MessageConstant.LOGIN_ERROR);
        }
        UserInfo userInfo = BeanUtil.copyProperties(user, UserInfo.class);
        String token = getToken(userInfo);
        BaseContext.setUserInfo(userInfo);
        return Result.success(new LoginInfo(token, userInfo));

    }

    private String getToken(UserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtTokenClaimsConstant.USER_ID, userInfo.getId());
        claims.put(JwtTokenClaimsConstant.OPEN_ID, userInfo.getOpenid());
        claims.put(JwtTokenClaimsConstant.NICK_NAME, userInfo.getNickname());
        return JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
    }

    /**
     * 用户微信登录
     * 一级登录,获取授权,默认
     *
     * @param userWechatDTO
     * @return
     */
    @Override
    public Result loginByWechat(UserWechatDTO userWechatDTO) {
        String code = userWechatDTO.getCode();
        JSONObject json = wechatLoginUtil.getWechatUserInfo(code);
        String openid = json.getStr("openid");
        if (openid == null) {
            return Result.error(MessageConstant.GET_OPENID_ERROR);
        }
        User user = this.lambdaQuery().eq(User::getOpenid, openid).one();
        //新用户
        if (user == null) {
            User userNew = new User();
            userNew.setOpenid(openid).setNickname(userWechatDTO.getNickName()).setAvatar(userWechatDTO.getAvatarUrl()).setFirstLoginTime(LocalDateTime.now()).setLastLoginTime(LocalDateTime.now());
            loginMapper.insert(userNew);
            UserInfo userInfo = BeanUtil.copyProperties(userNew, UserInfo.class);
            String token = getToken(userInfo);
            BaseContext.setUserInfo(userInfo);
            return Result.success(new LoginInfo(token, userInfo));
        }
        UserInfo userInfo = BeanUtil.copyProperties(user, UserInfo.class);
        String token = getToken(userInfo);
        BaseContext.setUserInfo(userInfo);
        return Result.success(new LoginInfo(token, userInfo));
    }

    /**
     * 获取登录用户信息
     *
     * @return
     */
    @Override
    public Result getUser() {
        return Result.success(BaseContext.getUserInfo());
    }

    /**
     * 创建账户
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public Result createAccount(String username, String password, String userType, String isEnterpriseAuth) {
        String hashPassword = BCrypt.hashpw(password);
        boolean isSuccess = save(User.builder().username(username).
                password(hashPassword).
                userType(UserType.getByDesc(userType)).
                isEnterpriseAuth(Integer.valueOf(isEnterpriseAuth)).
                firstLoginTime(LocalDateTime.now()).
                lastLoginTime(LocalDateTime.now()).
                build());
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success();
    }
}
