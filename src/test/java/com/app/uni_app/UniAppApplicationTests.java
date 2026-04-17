package com.app.uni_app;

import com.app.uni_app.aop.annotation.common.ParamCheckAnnotation;
import com.app.uni_app.common.generator.NicknameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UniAppApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(NicknameGenerator.generateDefaultNickname());
    }


    @ParamCheckAnnotation
    public void testParamCheck(Object object,String string,Integer integer){

    }
}
