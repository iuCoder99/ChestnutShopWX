package com.app.uni_app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.app.uni_app.mapper")
public class UniAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniAppApplication.class, args);
    }

}
