package com.app.uni_app.service.impl;

import com.app.uni_app.mapper.CouponUserMapper;
import com.app.uni_app.pojo.entity.CouponUser;
import com.app.uni_app.service.CouponUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CouponUserServiceImpl extends ServiceImpl<CouponUserMapper, CouponUser> implements CouponUserService {
}
