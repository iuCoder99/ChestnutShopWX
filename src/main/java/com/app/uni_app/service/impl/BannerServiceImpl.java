package com.app.uni_app.service.impl;

import com.app.uni_app.aop.annotation.RemoveBannerRedisCacheAnnotation;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.exception.JsonConvertException;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.redis.connect.StringRedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.mapper.BannerMapper;
import com.app.uni_app.pojo.dto.BannerDTO;
import com.app.uni_app.pojo.dto.BannerSortDTO;
import com.app.uni_app.pojo.dto.BannerStatusDTO;
import com.app.uni_app.pojo.emums.BannerStatus;
import com.app.uni_app.pojo.entity.Banner;
import com.app.uni_app.service.BannerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @description 针对表【banner(首页轮播图表)】的数据库操作Service实现
 * @createDate 2025-12-26 20:32:21
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner>
        implements BannerService {

    @Resource
    private CopyMapper copyMapper;

    @Resource
    private ObjectMapper objectMapper;

    private static final String DELETE_ID = "deleteId";
    private static final String BANNER_ID = "bannerId";
    private static final String SORT = "sort";
    private static final String STATUS = "status";

    /**
     * 获取首页轮播图列表
     *
     * @param pageNum  页码，表示请求的页数
     * @param pageSize 每页显示的数量
     * @return 返回包含轮播图数据的结果对象。成功时返回的数据结构中包含轮播图列表，失败时则包含错误信息。
     */
    @Override
    public Result<Object> getBannerList(Integer pageNum, Integer pageSize) {

        String bannerKey = RedisKeyGenerator.banner();
        String bannerJson = StringRedisConnector.opsForValue().get(bannerKey);
        List<Banner> bannerList;

        try {
            if (StringUtils.isBlank(bannerJson)) {
              bannerList = lambdaQuery()
                        .orderByAsc(Banner::getSort)
                        .page(new Page<>(pageNum, pageSize))
                        .getRecords().stream()
                        .filter(banner -> banner.getStatus().equals(BannerStatus.ACTIVE))
                        .toList();


                bannerJson = objectMapper.writeValueAsString(bannerList);

                if (bannerJson != null) {
                    StringRedisConnector.opsForValue().set(bannerKey, bannerJson);
                }

            } else {
                bannerList = objectMapper.readValue(bannerJson, new TypeReference<>() {});

            }
        } catch (JsonProcessingException e) {

            bannerList = lambdaQuery()
                    .orderByAsc(Banner::getSort)
                    .page(new Page<>(pageNum, pageSize))
                    .getRecords().stream()
                    .filter(banner -> banner.getStatus().equals(BannerStatus.ACTIVE))
                    .toList();

            StringRedisConnector.delete(bannerKey);

            throw new JsonConvertException(MessageConstant.JSON_CONVERT_ERROR);
        }


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




