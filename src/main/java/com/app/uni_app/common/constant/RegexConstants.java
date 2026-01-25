package com.app.uni_app.common.constant;

import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;

/**
 * 通用正则常量工具类
 * 设计原则：
 * 1. 常量类型：优先 public static final Pattern（编译后对象，高性能），配套 REGEX_XXX 字符串常量（复用场景）
 * 2. 命名规范：PATTERN_XXX（Pattern对象）、REGEX_XXX（字符串），前缀统一，后缀明确业务场景
 * 3. 场景覆盖：基础格式、业务核心（手机号/身份证/邮箱）、网络格式（IP/URL）、安全校验（密码）
 * 4. 安全校验：提供空值安全的静态方法（避免NPE），兼容 null/空串/空白符
 * 5. 性能优化：Pattern 静态编译（类加载时仅编译一次），高频调用无性能损耗
 * 6. 兼容性：JDK 17+，依赖 Commons Lang3（空值处理），可替换为 Spring StringUtils
 */
public final class RegexConstants {

    // ========================== 基础格式正则 ==========================
    /** 纯数字（字符串常量） */
    public static final String REGEX_DIGIT = "^\\d+$";
    /** 纯数字（Pattern对象，推荐使用） */
    public static final Pattern PATTERN_DIGIT = Pattern.compile(REGEX_DIGIT);

    /** 纯字母（大小写不限） */
    public static final String REGEX_LETTER = "^[a-zA-Z]+$";
    public static final Pattern PATTERN_LETTER = Pattern.compile(REGEX_LETTER);

    /** 字母+数字（不含特殊字符） */
    public static final String REGEX_LETTER_DIGIT = "^[a-zA-Z0-9]+$";
    public static final Pattern PATTERN_LETTER_DIGIT = Pattern.compile(REGEX_LETTER_DIGIT);

    /** 字母+数字+下划线（标识符规范） */
    public static final String REGEX_LETTER_DIGIT_UNDERLINE = "^[a-zA-Z0-9_]+$";
    public static final Pattern PATTERN_LETTER_DIGIT_UNDERLINE = Pattern.compile(REGEX_LETTER_DIGIT_UNDERLINE);

    /** 中文（仅Unicode中文，不含标点） */
    public static final String REGEX_CHINESE = "^[\\u4e00-\\u9fa5]+$";
    public static final Pattern PATTERN_CHINESE = Pattern.compile(REGEX_CHINESE);

    /** 中文+字母+数字（不含特殊字符） */
    public static final String REGEX_CHINESE_LETTER_DIGIT = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$";
    public static final Pattern PATTERN_CHINESE_LETTER_DIGIT = Pattern.compile(REGEX_CHINESE_LETTER_DIGIT);

    /** 空白符（空格、制表符、换行符等，纯空白串） */
    public static final String REGEX_WHITESPACE = "^\\s*$";
    public static final Pattern PATTERN_WHITESPACE = Pattern.compile(REGEX_WHITESPACE);

    // ========================== 业务核心正则 ==========================
    /** 手机号（中国大陆，13/14/15/16/17/18/19号段） */
    public static final String REGEX_PHONE = "^1[3-9]\\d{9}$";
    public static final Pattern PATTERN_PHONE = Pattern.compile(REGEX_PHONE);

    /** 固定电话（中国大陆，支持区号+号码，如010-12345678、02187654321） */
    public static final String REGEX_FIXED_PHONE = "^0\\d{2,3}-?\\d{7,8}$";
    public static final Pattern PATTERN_FIXED_PHONE = Pattern.compile(REGEX_FIXED_PHONE);

    /** 邮箱（标准格式，支持多级域名、下划线/中划线，如xxx_123@xxx.co.cn） */
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    public static final Pattern PATTERN_EMAIL = Pattern.compile(REGEX_EMAIL);

    /** 身份证号（18位，支持最后一位X/x，严格日期校验） */
    public static final String REGEX_ID_CARD_18 = "^[1-9]\\d{5}(19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
    public static final Pattern PATTERN_ID_CARD_18 = Pattern.compile(REGEX_ID_CARD_18);

    /** 身份证号（15位，旧版，仅数字） */
    public static final String REGEX_ID_CARD_15 = "^[1-9]\\d{5}\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}$";
    public static final Pattern PATTERN_ID_CARD_15 = Pattern.compile(REGEX_ID_CARD_15);

    /** 银行卡号（16-19位数字，主流银行通用） */
    public static final String REGEX_BANK_CARD = "^[1-9]\\d{15,18}$";
    public static final Pattern PATTERN_BANK_CARD = Pattern.compile(REGEX_BANK_CARD);

    /** 邮政编码（中国大陆，6位数字） */
    public static final String REGEX_POSTAL_CODE = "^[1-9]\\d{5}$";
    public static final Pattern PATTERN_POSTAL_CODE = Pattern.compile(REGEX_POSTAL_CODE);

    // ========================== 网络相关正则 ==========================
    /** IPV4地址（标准格式，如192.168.1.1、255.255.255.0，严格区间校验） */
    public static final String REGEX_IPV4 = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
    public static final Pattern PATTERN_IPV4 = Pattern.compile(REGEX_IPV4);

    /** URL（支持http/https/ftp，如 https: //www.baidu.com/path?a=1&b=2） */
    public static final String REGEX_URL = "^(http|https|ftp)://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$";
    public static final Pattern PATTERN_URL = Pattern.compile(REGEX_URL);

    /** 域名（不含协议，如www.baidu.com、blog.csdn.net） */
    public static final String REGEX_DOMAIN = "^([\\w-]+\\.)+[\\w-]+$";
    public static final Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

    // ========================== 安全校验正则 ==========================
    /** 密码（强密码：8-20位，包含大小写字母、数字、特殊字符至少三种） */
    public static final String REGEX_STRONG_PASSWORD = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,20}$";
    public static final Pattern PATTERN_STRONG_PASSWORD = Pattern.compile(REGEX_STRONG_PASSWORD);

    /** 密码（中强度：6-18位，字母+数字） */
    public static final String REGEX_MEDIUM_PASSWORD = "^[a-zA-Z0-9]{6,18}$";
    public static final Pattern PATTERN_MEDIUM_PASSWORD = Pattern.compile(REGEX_MEDIUM_PASSWORD);

    //=========================== 业务实用============================
    /**
     * 物流单号正则
     */
    public static final String REGEX_LOGISTICS_NO="^[A-Za-z0-9]{10,20}$";
    public static final Pattern PATTERN_LOGISTICS_NO=Pattern.compile(REGEX_LOGISTICS_NO);


    /**
     * 订单单号正则
     */
    public static final String REGEX_ORDER_NO="^\\d{6}-\\d{11}$";
    public static final Pattern PATTERN_ORDER_NO=Pattern.compile(REGEX_ORDER_NO);


    // ========================== 禁止实例化（工具类规范） ==========================
    private RegexConstants() {
        throw new AssertionError("工具类禁止实例化");
    }

    // ========================== 静态校验方法（空值安全） ==========================
    /**
     * 校验字符串是否符合指定正则（空值安全）
     * @param str 待校验字符串（可null/空串/空白符）
     * @param pattern 正则 Pattern对象
     * @return true：非空且符合正则；false：空值或不符合
     */
    public static boolean matches(String str, Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("正则 Pattern不可为null");
        }
        // 空值判断：isNotBlank 排除 null/空串/纯空白符（根据业务可改为 isNotEmpty）
        return StringUtils.isNotBlank(str) && pattern.matcher(str).matches();
    }

    // -------------------------- 业务便捷校验方法（无需传入Pattern） --------------------------
    /** 校验手机号 */
    public static boolean isPhone(String phone) {
        return matches(phone, PATTERN_PHONE);
    }

    /** 校验邮箱 */
    public static boolean isEmail(String email) {
        return matches(email, PATTERN_EMAIL);
    }

    /** 校验18位身份证号 */
    public static boolean isIdCard18(String idCard) {
        return matches(idCard, PATTERN_ID_CARD_18);
    }

    /** 校验强密码 */
    public static boolean isStrongPassword(String password) {
        return matches(password, PATTERN_STRONG_PASSWORD);
    }

    /** 校验IPV4地址 */
    public static boolean isIpv4(String ip) {
        return matches(ip, PATTERN_IPV4);
    }

    /** 校验物流单号 */
    public static boolean isLogisticsNo(String logisticsNo) {
        return matches(logisticsNo, PATTERN_LOGISTICS_NO);
    }

    /** 校验订单单号 */
    public static boolean isOrderNo(String orderNo) {
        return matches(orderNo, PATTERN_ORDER_NO);
    }


}