package com.blr19c.common.code;

/**
 * 生成雪花id
 *
 * @author blr
 */
public abstract class SnowflakeIdUtils {
    private static final SnowflakeIdUtils DEFAULT_INSTANCE = getInstance(1);

    public static SnowflakeIdUtils getInstance() {
        return DEFAULT_INSTANCE;
    }

    public static SnowflakeIdUtils getInstance(long workerId) {
        return new Snowflake(1623896691873L, workerId);
    }

    /**
     * @param epoch        开始时间戳
     * @param workerIdBits workerId占用位数
     * @param sequenceBits 序列号占用的位数
     * @param workerId     工作空间id
     */
    public static SnowflakeIdUtils getInstance(long epoch, long workerIdBits, long sequenceBits, long workerId) {
        return new Snowflake(epoch, workerIdBits, sequenceBits, workerId);
    }

    public static long defaultNextId() {
        return getInstance().getId();
    }

    public abstract long getId();

    public abstract long getWorkerId();
}
