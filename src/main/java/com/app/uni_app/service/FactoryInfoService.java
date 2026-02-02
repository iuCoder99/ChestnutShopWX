package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.entity.FactoryInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FactoryInfoService extends IService<FactoryInfo> {

    Result<Object> getFactoryInfo();

}
