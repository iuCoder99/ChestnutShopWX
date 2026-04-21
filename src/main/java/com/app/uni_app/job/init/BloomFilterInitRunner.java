package com.app.uni_app.job.init;

import com.app.uni_app.common.util.BloomFilterUtils;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BloomFilterInitRunner implements ApplicationRunner {

    @Resource
    private BloomFilterUtils bloomFilterUtils;

    @Resource
    private ProductService productService;


    @Override
    public void run(ApplicationArguments args) {
        log.info("初始化 布隆过滤器 ...");
        initBloomFilter();
        log.info("初始化 布隆过滤器 成功...");


    }


    private void initBloomFilter() {
        try {
            // 查询所有状态为启用的商品ID
            // 使用 listObjs 只查询 ID 字段，提高性能
            List<Object> productIds = productService.listObjs(
                    new LambdaQueryWrapper<Product>()
                            .select(Product::getId)
                            .eq(Product::getStatus, CommonStatus.ACTIVE)
            );

            if (productIds != null && !productIds.isEmpty()) {
                for (Object id : productIds) {
                    if (id instanceof Long) {
                        bloomFilterUtils.add((Long) id);
                    }
                }
                log.info("布隆过滤器初始化完成，共加载 {} 个商品ID", productIds.size());
            } else {
                log.info("暂无商品数据，跳过布隆过滤器初始化");
            }
        } catch (Exception e) {
            log.error("布隆过滤器初始化失败", e);
        }
    }

}
