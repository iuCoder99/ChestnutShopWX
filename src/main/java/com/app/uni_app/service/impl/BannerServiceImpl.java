package com.app.uni_app.service.impl;

import com.app.uni_app.aop.annotation.RemoveBannerRedisCacheAnnotation;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.mapper.BannerMapper;
import com.app.uni_app.pojo.dto.BannerDTO;
import com.app.uni_app.pojo.dto.BannerSortDTO;
import com.app.uni_app.pojo.dto.BannerStatusDTO;
import com.app.uni_app.pojo.emums.BannerStatus;
import com.app.uni_app.pojo.entity.Banner;
import com.app.uni_app.service.BannerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description 针对表【banner(首页轮播图表)】的数据库操作Service实现
 * @createDate 2025-12-26 20:32:21
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner>
        implements BannerService {

    @Resource
    private CopyMapper copyMapper;


    private static final String DELETE_ID = "deleteId";
    private static final String BANNER_ID = "bannerId";
    private static final String SORT = "sort";
    private static final String STATUS = "status";



    /**
     * 获取首页联播图
     * @return 联播图集合
     */
    @Override
    public Result<List<Banner>> getBannerList() {
        String bannerKey = RedisKeyGenerator.banner();
        List<Object> bannerList = RedisConnector.opsForList().range(bannerKey, 0, -1);

        if (Objects.isNull(bannerList) || bannerList.isEmpty()) {
            List<Banner> banners = lambdaQuery()
                    .orderByAsc(Banner::getSort)
                    .eq(Banner::getStatus, BannerStatus.ACTIVE)
                    .list();
            RedisConnector.delete(bannerKey);
            RedisConnector.opsForList().rightPushAll(bannerKey, banners.toArray());
            return Result.success(banners);
        }
        Collections.reverse(bannerList);
        List<Banner> resultList = bannerList.stream().map(object -> (Banner) object).toList();
        return Result.success(resultList);
    }

    /**
     * 管理员获取联播图
     * @return 全状态联播图列表
     */
    @Override
    public Result<List<Banner>> getBannerListAdmin(Integer pageNum , Integer pageSize) {
        List<Banner> bannerList = lambdaQuery().orderByAsc(Banner::getSort).list();
        return Result.success(bannerList);
    }

    /**
     * admin 添加 banner
     *
     * @param bannerDTO
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result addBanner(BannerDTO bannerDTO) {
        Banner banner = copyMapper.bannerDTOToBanner(bannerDTO);
        save(banner);
        return Result.success(banner);
    }

    /**
     * admin 修改 banner
     *
     * @param bannerDTO
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result updateBanner(BannerDTO bannerDTO) {
        Banner banner = copyMapper.bannerDTOToBanner(bannerDTO);
        updateById(banner);
        return Result.success(banner);
    }

    /**
     * admin 删除 banner
     *
     * @param id
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result deleteBanner(Long id) {
        removeById(id);
        HashMap<String, Object> resultMap = new HashMap<>(1);
        resultMap.put(DELETE_ID, id);
        return Result.success(resultMap);
    }

    /**
     * 更新 banner 排序
     *
     * @param bannerSortDTO
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result updateSort(BannerSortDTO bannerSortDTO) {
        lambdaUpdate().set(Banner::getSort, bannerSortDTO.getSort()).eq(Banner::getId, bannerSortDTO.getId()).update();
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(BANNER_ID, bannerSortDTO.getId());
        map.put(SORT, bannerSortDTO.getSort());
        return Result.success(map);
    }

    /**
     * 修改 banner 状态
     *
     * @param bannerStatusDTO
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result updateStatus(BannerStatusDTO bannerStatusDTO) {
        lambdaUpdate().set(Banner::getStatus, bannerStatusDTO.getStatus().getNumber())
                .eq(Banner::getId, bannerStatusDTO.getId()).update();
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(BANNER_ID, bannerStatusDTO.getId());
        map.put(STATUS, bannerStatusDTO.getStatus().getValue());
        return Result.success(map);
    }

}




