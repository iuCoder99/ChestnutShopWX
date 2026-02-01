package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.generator.NicknameGenerator;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.LoginInfo;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.result.UserInfo;
import com.app.uni_app.common.util.JwtUtils;
import com.app.uni_app.common.util.WechatLoginUtils;
import com.app.uni_app.mapper.SysUserMapper;
import com.app.uni_app.pojo.dto.UserDTO;
import com.app.uni_app.pojo.dto.UserWechatDTO;
import com.app.uni_app.pojo.entity.SysUser;
import com.app.uni_app.properties.JwtProperties;
import com.app.uni_app.service.SysLoginService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class SysLoginServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysLoginService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private WechatLoginUtils wechatLoginUtils;

    @Resource
    private CopyMapper copyMapper;

    private static final String OPEN_ID = "openid";

    /**
     * 用户登录
     * 二级登录,用户自选(可自由切换账号)
     *
     */
    @Override
    public Result loginByAccount(UserDTO userDTO) {
        String password = userDTO.getPassword();
        SysUser user = lambdaQuery().eq(SysUser::getUsername, userDTO.getUsername()).one();
        if (Objects.isNull(user)) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            return Result.error(MessageConstant.LOGIN_ERROR);
        }
        UserInfo userInfo = copyMapper.sysUserToUserInfo(user);
        String token = getToken(userInfo);
        BaseContext.setUserInfo(userInfo);
        return Result.success(new LoginInfo(token, userInfo));

    }

    private String getToken(UserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtTokenClaimsConstant.SYS_USER_ID, userInfo.getId());
        claims.put(JwtTokenClaimsConstant.OPEN_ID, userInfo.getOpenid());
        claims.put(JwtTokenClaimsConstant.NICK_NAME, userInfo.getNickname());
        return JwtUtils.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
    }

    /**
     * 用户微信登录
     * 一级登录,获取授权,默认
     *
     */
    @Override
    public Result loginByWechat(UserWechatDTO userWechatDTO) {

        String code = userWechatDTO.getCode();
        if (StringUtils.isBlank(code)) {
            return Result.error(MessageConstant.WECHAT_CODE_EMPTY);
        }

        JsonNode json = wechatLoginUtils.getWechatUserInfo(code);
        if (Objects.isNull(json) || !json.has(OPEN_ID)) {
            return Result.error(MessageConstant.GET_OPENID_ERROR);
        }

        String openid = json.get(OPEN_ID).asText();
        if (StringUtils.isBlank(openid)) {
            return Result.error(MessageConstant.GET_OPENID_ERROR);
        }

        SysUser user = this.lambdaQuery().eq(SysUser::getOpenid, openid).one();
        //新用户
        if (Objects.isNull(user)) {
            SysUser userNew = new SysUser();
            userNew.setOpenid(openid).setNickname(userWechatDTO.getNickName()).setAvatar(userWechatDTO.getAvatarUrl()).setFirstLoginTime(LocalDateTime.now()).setLastLoginTime(LocalDateTime.now());
            save(userNew);
            UserInfo userInfo = copyMapper.sysUserToUserInfo(userNew);
            String token = getToken(userInfo);
            BaseContext.setUserInfo(userInfo);
            return Result.success(new LoginInfo(token, userInfo));
        }
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
        UserInfo userInfo = copyMapper.sysUserToUserInfo(user);
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
     */
    @Override
    public Result createAccount(String username, String password, String phone) {
        SysUser user = lambdaQuery().eq(SysUser::getUsername, username).one();
        if (Objects.nonNull(user)) {
            return Result.error(MessageConstant.USER_NAME_EXISTS);
        }
        String nickname = NicknameGenerator.generateDefaultNickname();
        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        boolean isSuccess = save(SysUser.builder()
                .username(username)
                .password(hashPassword)
                .phone(phone)
                .nickname(nickname)
                .build());
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success();
    }


    /**
     * 忘记密码(使用手机号校验)
     *
     */
    @Override
    public Result forgetPassword(String username, String phone, String passwordNew) {
        SysUser user = lambdaQuery().eq(SysUser::getUsername, username).eq(SysUser::getPhone, phone).one();
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
     */
    @Override
    public Result changePassword(String username, String passwordOld, String passwordNew) {
        SysUser user = lambdaQuery().eq(SysUser::getUsername, username).one();
        if (Objects.isNull(user)) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!BCrypt.checkpw(passwordOld, user.getPassword())) {
            return Result.error(MessageConstant.LOGIN_ERROR);
        }
        String hashPasswordNew = BCrypt.hashpw(passwordNew, BCrypt.gensalt());
        user.setPassword(hashPasswordNew);
        boolean isSuccess = updateById(user);
        if (!isSuccess) {
            return Result.error(MessageConstant.PASSWORD_MODIFY_ERROR);
        }
        return Result.success(user.getId());

    }

    /**
     * 根据 username 查询 user 带角色和权限
     */
    @Override
    public SysUser getSysUserByNameWithRolesAndPermissions(String username) {
        if (StringUtils.isBlank(username)) {
            return null;

        }
        return sysUserMapper.getSysUserByNameWithRolesAndPermissions(username);
    }

    /**
     * 根据 userId 查询 user 带角色和权限
     */
    @Override
    public SysUser getSysUserByUserIdWithRolesAndPermissions(Long userId) {
        if (Objects.isNull(userId)) {
            return null;

        }
        return sysUserMapper.getSysUserByUserIdWithRolesAndPermissions(userId);
    }

    /**
     * 根据 userId 查询 user
     */
    @Override
    public SysUser getSimpleSysUserByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return null;

        }
        return lambdaQuery().eq(SysUser::getId, userId).one();
    }
}
