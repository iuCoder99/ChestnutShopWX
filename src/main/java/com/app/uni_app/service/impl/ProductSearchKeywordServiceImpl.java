package com.app.uni_app.service.impl;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.util.CaffeineUtils;
import com.app.uni_app.mapper.ProductSearchKeywordMapper;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.entity.ProductSearchKeyword;
import com.app.uni_app.service.ProductSearchKeywordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductSearchKeywordServiceImpl extends ServiceImpl<ProductSearchKeywordMapper, ProductSearchKeyword> implements ProductSearchKeywordService {
    @Resource
    private CaffeineUtils caffeineUtils;

    /**
     * 用户获取热门搜索关键词列表
     * @return
     */
    @Override
    public Result getProductSearchKeywordListUser() {
        List<String> hotProductSearchKeyword = caffeineUtils.getHotProductSearchKeyword();
        Collections.shuffle(hotProductSearchKeyword);
        List<String> resultList = hotProductSearchKeyword.stream().limit(5).toList();
        return Result.success(resultList);
    }

    public List<String> getHotProductSearchKeywordListUser(String cacheKey) {
        List<ProductSearchKeyword> list = lambdaQuery()
                .eq(ProductSearchKeyword::getIsShow, CommonStatus.ACTIVE.getNumber())
                .eq(ProductSearchKeyword::getIsHot, CommonStatus.ACTIVE.getNumber()).list();
        if (Objects.isNull(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(ProductSearchKeyword::getKeyword).collect(Collectors.toList());
    }

    /**
     * 管理员获取搜索关键词列表
     * @return
     */
    @Override
    public Result getProductSearchKeywordListAdmin() {
        List<ProductSearchKeyword> resultList = lambdaQuery().list();
        return Result.success(resultList);
    }

    /**
     * 管理员修改搜索关键词
     * @param productSearchKeywordList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateProductSearchListAdmin(List<ProductSearchKeyword> productSearchKeywordList) {
        remove(new LambdaQueryWrapper<>());
        saveBatch(productSearchKeywordList);
        caffeineUtils.invalidateHotProductSearchKeywordCache();
        return Result.success();
    }
}