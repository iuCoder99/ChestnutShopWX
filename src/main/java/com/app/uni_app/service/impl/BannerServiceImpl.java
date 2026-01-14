package com.app.uni_app.service.impl;

import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.BannerDTO;
import com.app.uni_app.pojo.dto.BannerSortDTO;
import com.app.uni_app.pojo.dto.BannerStatusDTO;
import com.app.uni_app.pojo.entity.Banner;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.uni_app.service.BannerService;
import com.app.uni_app.mapper.BannerMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @description 针对表【banner(首页轮播图表)】的数据库操作Service实现
 * @createDate 2025-12-26 20:32:21
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner>
        implements BannerService {

    private CopyMapper copyMapper;

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
    public Result getBannerList(Integer pageNum, Integer pageSize) {
        Page<Banner> page = lambdaQuery().orderByAsc(Banner::getSort).page(new Page<>(pageNum, pageSize));
        return Result.success(page.getRecords());
    }

    /**
     * admin 添加 banner
     *
     * @param bannerDTO
     * @return
     */
    @Override
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
    public Result updateStatus(BannerStatusDTO bannerStatusDTO) {
        lambdaUpdate().set(Banner::getStatus, bannerStatusDTO.getStatus().getNumber())
                .eq(Banner::getId, bannerStatusDTO.getId()).update();
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(BANNER_ID, bannerStatusDTO.getId());
        map.put(STATUS, bannerStatusDTO.getStatus().getValue());
        return Result.success(map);
    }
}




