package com.app.uni_app;


import com.app.uni_app.common.generator.SnowflakeIdGenerator;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test {
    private SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator();

    @org.junit.jupiter.api.Test
    void test1() {
        String id = snowflakeIdGenerator.generateOrderNo();
        System.out.println(id);
        //260121-37216224030
    }
}
