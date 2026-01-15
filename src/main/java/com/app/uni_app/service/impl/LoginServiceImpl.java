package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.LoginInfo;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.result.UserInfo;
import com.app.uni_app.common.utils.JwtUtil;
import com.app.uni_app.common.utils.WechatLoginUtil;
import com.app.uni_app.mapper.LoginMapper;
import com.app.uni_app.pojo.dto.UserDTO;
import com.app.uni_app.pojo.dto.UserWechatDTO;
import com.app.uni_app.pojo.entity.User;
import com.app.uni_app.properties.JwtProperties;
import com.app.uni_app.service.LoginService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class LoginServiceImpl extends ServiceImpl<LoginMapper, User> implements LoginService {

    @Resource
    private LoginMapper loginMapper;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private WechatLoginUtil wechatLoginUtil;

    @Resource
    private CopyMapper copyMapper;

    private static final String OPEN_ID = "openid";

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
        if (Objects.isNull(user)) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            return Result.error(MessageConstant.LOGIN_ERROR);
        }
        UserInfo userInfo = copyMapper.userToUserInfo(user);
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
        JsonNode json = wechatLoginUtil.getWechatUserInfo(code);
        JsonNode jsonNode = json.get(OPEN_ID);
        String openid = jsonNode == null ? null : jsonNode.asText();
        if (StringUtils.isBlank(openid)) {
            return Result.error(MessageConstant.GET_OPENID_ERROR);
        }
        User user = this.lambdaQuery().eq(User::getOpenid, openid).one();
        //新用户
        if (Objects.isNull(user)) {
            User userNew = new User();
            userNew.setOpenid(openid).setNickname(userWechatDTO.getNickName()).setAvatar(userWechatDTO.getAvatarUrl()).setFirstLoginTime(LocalDateTime.now()).setLastLoginTime(LocalDateTime.now());
            loginMapper.insert(userNew);
            UserInfo userInfo = copyMapper.userToUserInfo(userNew);
            String token = getToken(userInfo);
            BaseContext.setUserInfo(userInfo);
            return Result.success(new LoginInfo(token, userInfo));
        }
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
        UserInfo userInfo = copyMapper.userToUserInfo(user);
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
     * 创建普通用户账户
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public Result createAccount(String username, String password, String phone) {
        User user = lambdaQuery().eq(User::getUsername, username).one();
        if (Objects.nonNull(user)) {
            return Result.error(MessageConstant.USER_NAME_EXISTS);
        }
        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        boolean isSuccess = save(User.builder().username(username).
                password(hashPassword).
                phone(phone).
                build());
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success();
    }


    /**
     * 忘记密码(使用手机号校验)
     *
     * @param username
     * @param phone
     * @param passwordNew
     * @return
     */
    @Override
    public Result forgetPassword(String username, String phone, String passwordNew) {
        User user = lambdaQuery().eq(User::getUsername, username).eq(User::getPhone, phone).one();
        if (Objects.isNull(user)) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        String hashPasswordNew = BCrypt.hashpw(passwordNew, BCrypt.gensalt());
        user.setPassword(hashPasswordNew);
        boolean isSuccess = updateById(user);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(user.getId());
    }


    /**
     * 修改密码
     *
     * @param username
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @Override
    public Result changePassword(String username, String passwordOld, String passwordNew) {
        User user = lambdaQuery().eq(User::getUsername, username).one();
        if (Objects.isNull(user)) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!BCrypt.checkpw(passwordOld, user.getPassword())){
         return Result.error(MessageConstant.LOGIN_ERROR);
        }
        String hashPasswordNew = BCrypt.hashpw(passwordNew, BCrypt.gensalt());
        user.setPassword(hashPasswordNew);
        boolean isSuccess = updateById(user);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(user.getId());

    }
}
