package com.app.uni_app.common.generator;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法工具类（基于Spring+Apache Commons实现）
 *
 */
@Component
public class SnowflakeIdGenerator {
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    /**
     * 起始时间戳（自定义，建议设置为项目上线时间，减少ID长度）
     * 2026-01-01 00:00:00 的毫秒级时间戳
     */
    private static final long START_TIMESTAMP = 1777824000000L;

    /**
     * 机器ID位数（5位，支持0-31）
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心ID位数（5位，支持0-31）
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 序列号位数（12位，支持每毫秒生成4096个ID）
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID最大值（31）
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 数据中心ID最大值（31）
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 序列号最大值（4095）
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器ID左移位数（12位）
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID左移位数（17位）
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移位数（22位）
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 数据中心ID（自定义，根据部署环境配置）
     */
    private final long datacenterId;

    /**
     * 机器ID（自定义，根据部署机器配置）
     */
    private final long workerId;

    /**
     * 序列号（原子类保证并发安全）
     */
    private final AtomicLong sequence = new AtomicLong(0L);

    /**
     * 上一次生成 ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 构造方法（默认配置：数据中心ID=1，机器ID=1，小集群可直接用）
     * 分布式部署时建议通过配置文件注入workerId和datacenterId
     */
    public SnowflakeIdGenerator() {
        this(1L, 1L);
    }

    /**
     * 自定义机器 ID和数据中心 ID
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        // 校验参数合法性（Spring的Assert工具）
        Assert.isTrue(workerId >= 0 && workerId <= MAX_WORKER_ID, "Worker ID超出范围(0-31)");
        Assert.isTrue(datacenterId >= 0 && datacenterId <= MAX_DATACENTER_ID, "Datacenter ID超出范围(0-31)");
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个雪花ID（Long类型）
     */
    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();

        // 处理时钟回拨问题
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException(String.format("系统时钟回拨！拒绝生成ID，当前时间：%d，上一次时间：%d", currentTimestamp, lastTimestamp));
        }

        // 同一毫秒内，序列号自增
        if (currentTimestamp == lastTimestamp) {
            sequence.set((sequence.get() + 1) & MAX_SEQUENCE);
            // 同一毫秒内序列号耗尽，等待下一毫秒
            if (sequence.get() == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为0
            sequence.set(0L);
        }

        lastTimestamp = currentTimestamp;

        // 拼接雪花ID：时间戳 + 数据中心ID + 机器ID + 序列号
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence.get();
    }

    /**
     * 生成雪花 ID的字符串形式
     */
    public String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 生成订单编号（格式：年月日 + 雪花ID后12位）
     * 适配你的需求：年月日+随机数（雪花ID替代随机数，保证唯一）
     */
    public String generateOrderNo() {
        // Spring原生DateTimeFormatter（线程安全，替代SimpleDateFormat）
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");
        String datePart = LocalDateTime.now().format(dateFormatter);

        // 生成雪花ID并截取后12位（缩短长度，仍保证唯一）
        String snowflakePart = StringUtils.rightPad(String.valueOf(nextId()), 12, "0").substring(0, 12);

        // 拼接订单编号
        return datePart+snowflakePart;
    }

    /**
     * 获取当前毫秒级时间戳（Spring原生工具）
     */
    private long getCurrentTimestamp() {
        return LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 等待下一毫秒，直到获取新的时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
}
