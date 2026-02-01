package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.mapper.SysUserMapper;
import com.app.uni_app.pojo.dto.UserDetailDTO;
import com.app.uni_app.pojo.entity.SysUser;
import com.app.uni_app.pojo.vo.UserDetailVO;
import com.app.uni_app.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {
    @Resource
    private CopyMapper copyMapper;

    /**
     * 获取用户详情
     *
     * @return
     */
    @Override
    public Result getUserDetail() {
        String userId = BaseContext.getUserId();
        SysUser user = lambdaQuery().eq(SysUser::getId, userId).one();
        UserDetailVO userDetailVO = copyMapper.sysUserToUserDetailVO(user);
        return Result.success(userDetailVO);
    }

    /**
     * 修改用户详情
     *
     * @param userDetailDTO
     * @return
     */
    @Override
    public Result updateUserDetail(UserDetailDTO userDetailDTO) {
        String userId = BaseContext.getUserId();
        if (StringUtils.isBlank(userDetailDTO.getNickname())) {
            return Result.error(MessageConstant.USER_NAME_NOT_NULL);
        }
        boolean isSuccess = lambdaUpdate().eq(SysUser::getId, userId).set(SysUser::getNickname, userDetailDTO.getNickname())
                .set(SysUser::getAvatar, userDetailDTO.getAvatar()).set(SysUser::getPhone, userDetailDTO.getPhone()).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success();
    }
}
