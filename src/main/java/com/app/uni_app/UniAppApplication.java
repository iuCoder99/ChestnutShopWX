package com.app.uni_app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.app.uni_app.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class UniAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniAppApplication.class, args);
    }

}
