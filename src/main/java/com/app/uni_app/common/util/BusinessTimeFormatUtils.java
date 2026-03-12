package com.app.uni_app.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class BusinessTimeFormatUtils {

    // 定义日期格式化器（两位年+两位月+两位日，补零）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy年MM月dd日");

    /**
     * 计算两个时间的差值，并返回时间描述字符串（新业务规则）
     * 边界规则：
     * 时间差 < 1 分钟 → 显示「刚刚」
     * 1 分钟 ≤ 时间差 < 12 小时 → 显示「X 小时前」（不足 1 小时按 1 小时算）
     * 12 小时 ≤ 时间差 < 24 小时 → 显示「一天内」
     * 24 小时 ≤ 时间差 < 10 天 → 显示「X 天前」
     * 10 天 ≤ 时间差 < 30 天 → 显示「一个月内」
     * 时间差 ≥ 30 天 → 显示「YY年MM月dd日」（如25年12月02日）
     *
     * @param startTime 起始时间（如原评论创建时间）
     * @param endTime   结束时间（如追评创建时间/当前时间）
     * @return 格式化后的时间描述
     */
    public static String formatTimeDiff(LocalDateTime startTime, LocalDateTime endTime) {
        // 空值校验：如果起始时间或结束时间为空，默认返回"刚刚"
        if (startTime == null || endTime == null) {
            return "刚刚";
        }

        // 确保计算的是 endTime - startTime（避免负数，保证时间差为正）
        if (endTime.isBefore(startTime)) {
            LocalDateTime temp = startTime;
            startTime = endTime;
            endTime = temp;
        }

        // 1. 计算时间差（分钟）：核心基准值
        long minutesDiff = ChronoUnit.MINUTES.between(startTime, endTime);

        // 规则1：小于1分钟 → 刚刚
        if (minutesDiff < 1) {
            return "刚刚";
        }

        // 2. 计算小时差（用于12小时/24小时区间判断）
        long hoursDiff = ChronoUnit.HOURS.between(startTime, endTime);

        // 规则2：1分钟 ≤ 时间差 < 12小时 → X小时前（不足1小时按1小时算）
        if (hoursDiff < 12) {
            long showHours = hoursDiff == 0 ? 1 : hoursDiff; // 59分钟也显示1小时前
            return showHours + "小时前";
        }

        // 规则3：12小时 ≤ 时间差 < 24小时 → 一天内
        if (hoursDiff < 24) {
            return "一天内";
        }

        // 3. 计算天数差（用于10天/30天区间判断）
        long daysDiff = ChronoUnit.DAYS.between(startTime, endTime);

        // 规则4：24小时 ≤ 时间差 < 10天 → X天前
        if (daysDiff < 10) {
            return daysDiff + "天前";
        }

        // 规则5：10天 ≤ 时间差 < 30天 → 一个月内
        if (daysDiff < 30) {
            return "一个月内";
        }

        // 规则6：≥30天 → 格式化显示「YY年MM月dd日」（取起始时间的年月日）
        return startTime.format(DATE_FORMATTER);
    }

    // 重载方法：直接传入起始时间，对比当前时间
    public static String formatTimeDiffWithNow(LocalDateTime startTime) {
        return formatTimeDiff(startTime, LocalDateTime.now());
    }
}