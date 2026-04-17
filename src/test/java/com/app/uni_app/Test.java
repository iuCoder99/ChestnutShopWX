package com.app.uni_app;


import com.app.uni_app.common.generator.SnowflakeIdGenerator;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.repository.ProductEsRepository;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.pojo.entity.ProductSpec;
import com.app.uni_app.service.impl.CouponServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class Test {
    private final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator();

    @Resource
    private CouponServiceImpl couponService;

    @Resource
    private ProductEsRepository productEsRepository;

    @org.junit.jupiter.api.Test
    void test1() {
        String id = snowflakeIdGenerator.generateOrderNo();
        System.out.println(id);
        //260121-37216224030
    }

    @org.junit.jupiter.api.Test
    void test2() {
        Product product = new Product();
        ProductSpec productSpec = ProductSpec.builder().productId(1L).price(BigDecimal.ONE).stock(1111).build();
        product.setSpecList(List.of(productSpec));
        RedisConnector.setHashObject("test", product);

    }

    @org.junit.jupiter.api.Test
    void test3() {
        System.out.println("================== 调试开始 ==================");
        String fieldName = Product.Fields.specList;
        System.out.println("期望获取的字段名: [" + fieldName + "]");
        System.out.println("字段名长度: " + fieldName.length());

        // 1. 检查 entries 中的 key 是否真的包含该字段
        Map<String, Object> entries = RedisConnector.opsForHash().entries("test");
        System.out.println("Redis 中所有的 Key: " + entries.keySet());

        boolean found = false;
        for (String k : entries.keySet()) {
            if (k instanceof String) {
                String sk = k;
                if (sk.equals(fieldName)) {
                    System.out.println("找到完全匹配的 Key: [" + sk + "]");
                    found = true;
                } else if (sk.contains(fieldName)) {
                    System.out.println("找到部分包含的 Key: [" + sk + "], 长度: " + sk.length());
                }
            } else {
                System.out.println("Key 不是 String 类型: " + k.getClass().getName());
            }
        }

        if (!found) {
            System.out.println("警告：未找到完全匹配的 Key [" + fieldName + "]");
        }

        // 2. 尝试直接获取
        Object rawValue = RedisConnector.opsForHash().get("test", fieldName);
        System.out.println("1.RedisConnector.opsForHash().get(\"test\", \"" + fieldName + "\") 结果: " + rawValue);

        // 3. 尝试使用硬编码字符串获取
        Object hardCodedValue = RedisConnector.opsForHash().get("test", "specList");
        System.out.println("RedisConnector.opsForHash().get(\"test\", \"specList\") 结果: " + hardCodedValue);

        // 4. 再次尝试 getHashField
        List<ProductSpec> specList = RedisConnector.getHashField("test", fieldName, new TypeReference<List<ProductSpec>>() {
        });
        System.out.println("2.RedisConnector.getHashField 结果: " + specList);

        Product product = RedisConnector.getHashObject("test", Product.class);
        System.out.println("RedisConnector.getHashObject 结果: " + product);
        if (product != null) {
            System.out.println("product.getSpecList(): " + product.getSpecList());
        }
        System.out.println("================== 调试结束 ==================");
    }

    @org.junit.jupiter.api.Test
    public void test4() {
        couponService.updateCouponRedisCache();
    }

    @org.junit.jupiter.api.Test
    public void testEsRepository(){
        ProductDocument one = productEsRepository.getById(1L);
        System.out.println("单个查询数据:"+one.toString());

        ProductDocument oneNotHave = productEsRepository.getById(0L);
        System.out.println("查询不存在的数据" + oneNotHave);
        List<ProductDocument> two = productEsRepository.getById(1L, 2L);
        System.out.println("定参数批量查询"+two.get(0).toString()+two.get(1).toString());

        List<ProductDocument> isOne = productEsRepository.getById(0L, 1L);
        System.out.println("查询数两个,实际存在一个,测试查询"+isOne.size());

        List<Long> list = List.of(2L, 3L, 4L);
        List<ProductDocument> queryList = productEsRepository.getByIdList(list);
        System.out.println("列表 id 查询 查询出数量+"+ queryList.size()+";期望数量3");
        UniAppApplicationTests uniAppApplicationTests = new UniAppApplicationTests();
        uniAppApplicationTests.testParamCheck(null,null,null);

    }


}
