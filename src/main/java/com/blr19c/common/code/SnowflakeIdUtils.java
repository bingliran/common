package com.blr19c.common.code;

import java.util.Random;

/**
 * 生成雪花id
 *
 * @author blr
 */
public abstract class SnowflakeIdUtils {


    public static SnowflakeIdUtils getInstance() {
        return Snowflake.DEFAULT_INSTANCE;
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

    public static class Snowflake extends SnowflakeIdUtils {
        private static final SnowflakeIdUtils DEFAULT_INSTANCE = getInstance(1);

        private static final Random RANDOM = new Random();
        /**
         * 起始的时间戳
         */
        private final long epoch;
        /**
         * workerId偏移量
         */
        private final long workerIdShift;
        /**
         * 时间偏移量
         */
        private final long timestampLeftShift;
        /**
         * 同毫秒内序列
         */
        private final long sequenceMask;
        /**
         * 工作空间id
         */
        private final long workerId;

        private long sequence = 0L;
        private long lastTimestamp = -1L;

        public Snowflake(long epoch, long workerId) {
            this(epoch, 10, 12, workerId);
        }

        /**
         * @param epoch        开始时间戳
         * @param workerIdBits workerId占用位数
         * @param sequenceBits 序列号占用的位数
         * @param workerId     workerId
         */
        public Snowflake(long epoch, long workerIdBits, long sequenceBits, long workerId) {
            this.epoch = epoch;
            //最大能够分配的workerId
            long maxWorkerId = ~(-1L << workerIdBits);
            if (workerId <= 0 || workerId >= maxWorkerId) {
                throw new IllegalArgumentException("workerId <= 0 || workerId >= " + maxWorkerId);
            }
            this.workerIdShift = sequenceBits;
            this.timestampLeftShift = sequenceBits + workerIdBits;
            this.sequenceMask = ~(-1L << sequenceBits);
            this.workerId = workerId;
        }

        public long getWorkerId() {
            return workerId;
        }

        public synchronized long getId() {
            long timestamp = timeGen();
            if (timestamp < lastTimestamp) {
                long offset = lastTimestamp - timestamp;
                if (offset <= 5) {
                    try {
                        wait(offset << 1);
                        timestamp = timeGen();
                        if (timestamp < lastTimestamp) {
                            throw new IllegalStateException(String.format(
                                    "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                                    lastTimestamp - timestamp));
                        }
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    throw new IllegalStateException(String.format(
                            "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp));
                }
            }
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    //序列溢出等待下一个毫秒
                    sequence = RANDOM.nextInt(100);
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                //如果是新的
                sequence = RANDOM.nextInt(100);
            }
            lastTimestamp = timestamp;
            return ((timestamp - epoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        }

        private long tilNextMillis(long lastTimestamp) {
            long timestamp = timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = timeGen();
            }
            return timestamp;
        }

        private long timeGen() {
            return System.currentTimeMillis();
        }
    }
}
