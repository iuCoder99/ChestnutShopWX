package com.app.uni_app.common.util;

import com.app.uni_app.common.constant.DatePatternConstants;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * 企业级时间转换工具类
 */
public class DateUtils {
    // ==================== 格式化（时间对象 → 字符串）====================
    /**
     * LocalDateTime 转 标准日期时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null; // 或返回空串，根据业务约定
        }
        return localDateTime.format(DatePatternConstants.NORMAL_DATETIME_FORMATTER);
    }

    /**
     * LocalDate 转 标准日期字符串（yyyy-MM-dd）
     */
    public static String formatLocalDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(DatePatternConstants.NORMAL_DATE_FORMATTER);
    }

    /**
     * 带时区时间（ZonedDateTime）转 标准字符串（含时区信息，如：2024-01-22 10:00:00 +08:00）
     */
    public static String formatZonedDateTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        // 自定义带时区的格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX");
        return zonedDateTime.format(formatter);
    }

    // ==================== 解析（字符串 → 时间对象）====================
    /**
     * 标准日期时间字符串（yyyy-MM-dd HH:mm:ss）转 LocalDateTime
     * 企业规范：必须捕获解析异常，避免非法字符串导致崩溃
     */
    public static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), DatePatternConstants.NORMAL_DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // 企业规范：日志记录异常（便于排查），返回null或默认值
            System.err.println("解析日期时间失败，输入字符串：" + dateTimeStr + "，异常信息：" + e.getMessage());
            return null;
        }
    }

    /**
     * 标准日期字符串（yyyy-MM-dd）转 LocalDate
     */
    public static LocalDate parseToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DatePatternConstants.NORMAL_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("解析日期失败，输入字符串：" + dateStr + "，异常信息：" + e.getMessage());
            return null;
        }
    }

    // ==================== 时间戳 与 时间对象互转 ====================
    /**
     * 毫秒时间戳 → LocalDateTime（系统时区：Asia/Shanghai）
     */
    public static LocalDateTime timestampToLocalDateTime(long timestampMs) {
        return Instant.ofEpochMilli(timestampMs)
                .atZone(DatePatternConstants.SYSTEM_ZONE_ID)
                .toLocalDateTime();
    }

    /**
     * LocalDateTime → 毫秒时间戳（系统时区）
     */
    public static long localDateTimeToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0L;
        }
        return localDateTime.atZone(DatePatternConstants.SYSTEM_ZONE_ID)
                .toInstant()
                .toEpochMilli();
    }

    // ==================== 时区转换（跨系统交互必备）====================
    /**
     * LocalDateTime + 系统时区 → ZonedDateTime（如：转为UTC时间）
     */
    public static ZonedDateTime localDateTimeToZonedDateTime(LocalDateTime localDateTime, ZoneId targetZone) {
        if (localDateTime == null || targetZone == null) {
            return null;
        }
        // 先绑定系统时区，再转换到目标时区
        return localDateTime.atZone(DatePatternConstants.SYSTEM_ZONE_ID)
                .withZoneSameInstant(targetZone);
    }

    // ==================== 日期计算（企业业务常用）====================
    /**
     * 计算两个LocalDateTime的时间差（单位：指定时间单位，如天、小时）
     */
    public static long calculateTimeDiff(LocalDateTime start, LocalDateTime end, ChronoUnit unit) {
        if (start == null || end == null || unit == null) {
            return 0L;
        }
        // 确保end >= start，避免负数（根据业务调整）
        if (end.isBefore(start)) {
            LocalDateTime temp = start;
            start = end;
            end = temp;
        }
        return ChronoUnit.MILLIS.between(start, end) / unit.getDuration().toMillis();
    }
}