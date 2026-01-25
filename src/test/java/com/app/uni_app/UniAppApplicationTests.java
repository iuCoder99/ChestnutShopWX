package com.app.uni_app;

import com.app.uni_app.common.generator.NicknameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UniAppApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(NicknameGenerator.generateDefaultNickname());
    }

}
