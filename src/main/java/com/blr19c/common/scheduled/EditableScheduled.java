package com.blr19c.common.scheduled;

import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.annotation.*;

/**
 * 可更新计划任务
 *
 * @author blr
 * @see Scheduled
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EditableScheduled {

    String CRON_DISABLED = Scheduled.CRON_DISABLED;
    String DEFAULT_LOCK_AT_LEAST_FOR = "PT30S";
    String DEFAULT_LOCK_AT_MOST_FOR = "PT1H";

    /**
     * cron表达式同时支持类似@Value("${xxx}") 配置文件写法 ${xxx}
     */
    String cron() default "";

    /**
     * 时区 默认为本服务器时区
     */
    String zone() default "";

    /**
     * 时间间隔 比如 fixedDelay=5000 在上次执行完成之后下一次开始将会在5秒之后
     */
    long fixedDelay() default -1;

    /**
     * 和fixedDelay基本一致 区别在于fixedDelayString支持Duration表达式和配置文件
     */
    String fixedDelayString() default "";

    /**
     * 和fixedDelay相似 区别在于 fixedRate在任务开始时计时
     * fixedRate=5000 但是执行耗时2000 那么下次开始将会在3秒后
     */
    long fixedRate() default -1;

    /**
     * 参考fixedDelay和fixedDelayString的区别
     */
    String fixedRateString() default "";

    /**
     * 首次执行之前延时的毫秒数
     */
    long initialDelay() default -1;

    /**
     * 参考fixedDelay和fixedDelayString的区别
     */
    String initialDelayString() default "";

    /**
     * 任务id 用于后续操作任务 默认则使用方法名
     */
    String workId() default "";

    /**
     * 是否开启多任务锁定
     *
     * @see LockableTaskScheduler
     */
    boolean enableSchedulerLock() default false;

    /**
     * enableSchedulerLock为true时此配置才会生效
     * SchedulerLock->name默认取值 workId
     * lockAtLeastFor 默认至少锁定30秒
     * lockAtMostFor 默认至多锁定1小时
     * 锁定配置运行时不可更改
     */
    SchedulerLock schedulerLock() default @SchedulerLock(lockAtLeastFor = DEFAULT_LOCK_AT_LEAST_FOR, lockAtMostFor = DEFAULT_LOCK_AT_MOST_FOR);
}
