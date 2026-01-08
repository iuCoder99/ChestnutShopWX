package com.app.uni_app;

import cn.hutool.crypto.digest.BCrypt;

public class Test {
   @org.junit.jupiter.api.Test
    public void test1(){
       System.out.println(BCrypt.hashpw("123456"));
    }
}
