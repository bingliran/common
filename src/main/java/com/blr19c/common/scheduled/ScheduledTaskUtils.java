package com.blr19c.common.scheduled;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 后操作任务
 */
public class ScheduledTaskUtils {

    static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    static Map<String, CopyReschedulingRunnable> taskMap = new ConcurrentHashMap<>();

    /**
     * 添加定时任务
     */
    public static void addTask(String workId, Runnable task, Trigger trigger, ErrorHandler errorHandler) {
        checkWork(workId, trigger);
        checkWork(task);
        CopyReschedulingRunnable copyReschedulingRunnable = getCopyReschedulingRunnable(task, trigger, errorHandler);
        CopyReschedulingRunnable currScheduling = taskMap.putIfAbsent(workId, copyReschedulingRunnable);
        //concurrent
        if (currScheduling != null && currScheduling != copyReschedulingRunnable)
            throw new IllegalArgumentException("workId already exists");
        if (currScheduling == null)
            copyReschedulingRunnable.schedule();
    }

    /**
     * 添加定时任务
     */
    public static void addTask(String workId, Runnable task, Trigger trigger) {
        addTask(workId, task, trigger, null);
    }

    /**
     * 删除任务
     *
     * @param mayInterruptIfRunning 是否立即使用Interrupt标记中断
     */
    public static void removeTask(String workId, boolean mayInterruptIfRunning) {
        checkWork(workId);
        CopyReschedulingRunnable copyReschedulingRunnable = taskMap.get(workId);
        if (copyReschedulingRunnable == null)
            throw new IllegalArgumentException("The workId(" + workId + ") no corresponding task");
        //已经被取消
        if (copyReschedulingRunnable.isCancelled()) {
            taskMap.remove(workId, copyReschedulingRunnable);
            return;
        }
        if (!copyReschedulingRunnable.isDone() && !mayInterruptIfRunning) {
            throw new IllegalArgumentException("The current task is running, workId:" + workId);
        }
        taskMap.remove(workId, copyReschedulingRunnable);
        copyReschedulingRunnable.cancel(mayInterruptIfRunning);
    }

    /**
     * 替换任务
     */
    public static void replaceTask(String workId, Trigger trigger, Runnable runnable) {
        checkWork(workId);
        CopyReschedulingRunnable copyReschedulingRunnable = taskMap.get(workId);
        if (copyReschedulingRunnable == null)
            throw new IllegalArgumentException("The workId(" + workId + ") no corresponding task");
        if (trigger != null)
            copyReschedulingRunnable.replaceTrigger(trigger);
        if (runnable != null)
            copyReschedulingRunnable.replaceDelegate(runnable);
    }

    /**
     * 替换任务(仅替换执行任务)
     */
    public static void replaceTask(String workId, Runnable runnable) {
        replaceTask(workId, null, runnable);
    }

    /**
     * 替换任务(仅替换执行时间)
     */
    public static void replaceTask(String workId, Trigger trigger) {
        replaceTask(workId, trigger, null);
    }

    private static void checkWork(String workId) {
        Assert.notNull(workId, "workId not be null");
    }

    private static void checkWork(Runnable runnable) {
        Assert.notNull(runnable, "runnable not be null");
    }

    private static void checkWork(String workId, Trigger trigger) {
        checkWork(workId);
        Assert.notNull(trigger, "trigger not be null");
    }

    private static CopyReschedulingRunnable getCopyReschedulingRunnable(Runnable task, Trigger trigger,
                                                                        ErrorHandler errorHandler) {
        errorHandler = errorHandler == null ? TaskUtils.getDefaultErrorHandler(true) : errorHandler;
        return new CopyReschedulingRunnable(task, trigger, executor, errorHandler);
    }
}
