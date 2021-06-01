package com.blr19c.common.scheduled;

import net.javacrumbs.shedlock.core.ClockProvider;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.javacrumbs.shedlock.support.annotation.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import static com.blr19c.common.scheduled.EditableRedisLockManager.LockAssert.alreadyLockedBy;
import static com.blr19c.common.scheduled.EditableScheduled.DEFAULT_LOCK_AT_LEAST_FOR;
import static com.blr19c.common.scheduled.EditableScheduled.DEFAULT_LOCK_AT_MOST_FOR;


/**
 * 锁定任务管理器
 *
 * @author blr
 */
public class EditableRedisLockManager {
    private final RedisLockProvider lockProvider;

    public EditableRedisLockManager(StringRedisTemplate redisTemplate) {
        this.lockProvider = new RedisLockProvider(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
    }

    private static Duration parseDuration(String value) throws RuntimeException {
        if (value.length() > 1 && (isP(value.charAt(0)) || isP(value.charAt(1)))) {
            return Duration.parse(value);
        }
        return Duration.ofMillis(Long.parseLong(value));
    }

    private static boolean isP(char ch) {
        return (ch == 'P' || ch == 'p');
    }

    public Runnable getLockRunnable(Runnable task, String workId, EditableScheduled scheduled) {
        SchedulerLock schedulerLock = scheduled.schedulerLock();
        LockConfiguration lockConfiguration = new LockConfiguration(
                ClockProvider.now(),
                StringUtils.hasText(schedulerLock.name()) ? schedulerLock.name() : workId,
                parseDuration(StringUtils.hasText(schedulerLock.lockAtMostFor()) ? schedulerLock.lockAtMostFor() : DEFAULT_LOCK_AT_MOST_FOR),
                parseDuration(StringUtils.hasText(schedulerLock.lockAtLeastFor()) ? schedulerLock.lockAtLeastFor() : DEFAULT_LOCK_AT_LEAST_FOR)
        );
        //LockRunnable is static class
        return new LockRunnable(task, lockProvider, lockConfiguration);
    }

    static class LockRunnable implements Runnable {

        final RedisLockProvider provider;
        final LockConfiguration lockConfiguration;
        volatile Runnable task;

        LockRunnable(Runnable task, RedisLockProvider provider, LockConfiguration lockConfiguration) {
            this.task = task;
            this.provider = provider;
            this.lockConfiguration = lockConfiguration;
        }

        @Override
        public void run() {
            String lockName = lockConfiguration.getName();
            Optional<SimpleLock> lock = provider.lock(lockConfiguration);
            //If the concurrency caused by spring has been used, avoid waiting for the lock again
            if (alreadyLockedBy(lockName)) {
                task.run();
            } else if (lock.isPresent()) {
                try {
                    LockAssert.startLock(lockName);
                    task.run();
                } finally {
                    LockAssert.endLock();
                    lock.get().unlock();
                }
            }
        }
    }

    /**
     * Prevent concurrent exceptions caused by spring
     */
    static class LockAssert {
        private static final ThreadLocal<String> currentLockName = ThreadLocal.withInitial(() -> null);

        static void startLock(String name) {
            currentLockName.set(name);
        }

        static boolean alreadyLockedBy(@NonNull String name) {
            return name.equals(currentLockName.get());
        }

        static void endLock() {
            currentLockName.remove();
        }
    }
}
