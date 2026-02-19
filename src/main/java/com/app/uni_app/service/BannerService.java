package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.BannerDTO;
import com.app.uni_app.pojo.dto.BannerSortDTO;
import com.app.uni_app.pojo.dto.BannerStatusDTO;
import com.app.uni_app.pojo.entity.Banner;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author 20589
 * @description 针对表【banner(首页轮播图表)】的数据库操作Service
 * @createDate 2025-12-26 20:32:21
 */
public interface BannerService extends IService<Banner> {
    Result getBannerList(Integer pageNum, Integer pageSize) throws JsonProcessingException;

    Result addBanner(BannerDTO bannerDTO);

    Result updateBanner(BannerDTO bannerDTO);

    Result deleteBanner(Long id);

    Result updateSort(BannerSortDTO bannerSortDTO);

    Result updateStatus(BannerStatusDTO bannerStatusDTO);
}

