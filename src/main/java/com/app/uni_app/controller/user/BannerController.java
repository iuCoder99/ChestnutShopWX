package com.app.uni_app.controller.user;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.BannerDTO;
import com.app.uni_app.pojo.dto.BannerSortDTO;
import com.app.uni_app.pojo.dto.BannerStatusDTO;
import com.app.uni_app.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BannerController {
    @Resource
    private BannerService bannerService;

    /**
     * 获取首页联播图列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/banner/list")
    public Result getBannerList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize) {
        return bannerService.getBannerList(pageNum, pageSize);
    }

    /**
     * 用于 admin 获取联播图列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/admin/banner/list")
    public Result getBannerListByAdmin(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize) {
        return bannerService.getBannerList(pageNum, pageSize);
    }

    /**
     * admin 添加 banner
     *
     * @return
     */
    @PostMapping("/admin/banner/add")
    public Result addBanner(@RequestBody BannerDTO bannerDTO) {
        return bannerService.addBanner(bannerDTO);
    }

    /**
     * admin 修改 banner
     *
     * @param bannerDTO
     * @return
     */
    @PutMapping("/admin/banner/update")
    public Result updateBanner(@RequestBody BannerDTO bannerDTO) {
        return bannerService.updateBanner(bannerDTO);
    }

    /**
     * admin 删除 banner
     *
     * @param id
     * @return
     */
    @DeleteMapping("/admin/banner/delete")
    public Result deleteBanner(@RequestParam Long id) {
        return bannerService.deleteBanner(id);
    }

    /**
     * 更新 Banner 排序
     * @param bannerSortDTO
     * @return
     */
    @PutMapping("/admin/banner/updateSort")
    public Result updateSort( @RequestBody BannerSortDTO bannerSortDTO) {
        return bannerService.updateSort(bannerSortDTO);
    }

    /**
     * 更新 banner 状态
     * @return
     */
    @PutMapping("/admin/banner/updateStatus")
    public Result updateStatus(@RequestBody BannerStatusDTO bannerStatusDTO){
        return bannerService.updateStatus(bannerStatusDTO);
    }


}
