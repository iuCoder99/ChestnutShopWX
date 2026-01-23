package com.app.uni_app.common.constant;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

/**
 * 时间格式全局常量（
 */
public class DatePatternConstants {

    public static final String DATE_TIME_FORM = "yyyy-MM-dd HH:mm:ss";



    // 标准日期时间（带秒）：yyyy-MM-dd HH:mm:ss
    public static final DateTimeFormatter NORMAL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 标准日期：yyyy-MM-dd
    public static final DateTimeFormatter NORMAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 标准时间（带毫秒）：HH:mm:ss.SSS
    public static final DateTimeFormatter NORMAL_TIME_WITH_MS_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    // ISO标准格式（无时区）：yyyy-MM-dd 'T' HH:mm:ss（接口交互常用）
    public static final DateTimeFormatter ISO_LOCAL_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 系统默认时区（显式指定，避免依赖服务器环境）
    public static final ZoneId SYSTEM_ZONE_ID = ZoneId.of("Asia/Shanghai");
    // UTC时区（跨系统交互、日志存储推荐）
    public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
}