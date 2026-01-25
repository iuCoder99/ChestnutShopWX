package com.app.uni_app.common.generator;

import org.apache.commons.lang3.RandomStringUtils;

public class NicknameGenerator {
    private static final String PREFIX = "用户";
    private static final int RANDOM_LENGTH = 6;

    /**
     * 生成默认昵称（无重复校验）
     */
    public static String generateDefaultNickname() {
        String randomStr = RandomStringUtils.randomAlphanumeric(RANDOM_LENGTH);
        return  PREFIX+ randomStr;
    }


}
