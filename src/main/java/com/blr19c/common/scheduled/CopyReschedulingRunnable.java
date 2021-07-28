package com.blr19c.common.scheduled;

import com.blr19c.common.code.ReflectionUtils;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

import javax.annotation.Nonnull;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.concurrent.*;


/**
 * 周期执行器
 * schedule -> executor-> nextExecutionTime -> initialDelay-> a long time later -> run -> isCancelled -> schedule
 *
 * @author blr
 */
public class CopyReschedulingRunnable implements ScheduledFuture<Object>, Runnable {

    private final ErrorHandler errorHandler;
    private final SimpleTriggerContext triggerContext = new SimpleTriggerContext();
    private final ScheduledExecutorService executor;
    private final Object triggerContextMonitor = new Object();
    private volatile Trigger trigger;
    private volatile Runnable delegate;
    @Nullable
    private ScheduledFuture<?> currentFuture;
    @Nullable
    private Date scheduledExecutionTime;


    public CopyReschedulingRunnable(
            Runnable delegate, Trigger trigger, ScheduledExecutorService executor, ErrorHandler errorHandler) {
        Assert.notNull(delegate, "Delegate must not be null");
        Assert.notNull(errorHandler, "ErrorHandler must not be null");
        this.delegate = delegate;
        this.errorHandler = errorHandler;
        this.trigger = trigger;
        this.executor = executor;
    }


    @Nullable
    public ScheduledFuture<?> schedule() {
        synchronized (this.triggerContextMonitor) {
            this.scheduledExecutionTime = this.trigger.nextExecutionTime(this.triggerContext);
            if (this.scheduledExecutionTime == null) {
                return null;
            }
            long initialDelay = this.scheduledExecutionTime.getTime() - System.currentTimeMillis();
            this.currentFuture = this.executor.schedule(this, initialDelay, TimeUnit.MILLISECONDS);
            return this;
        }
    }

    private ScheduledFuture<?> obtainCurrentFuture() {
        Assert.state(this.currentFuture != null, "No scheduled future");
        return this.currentFuture;
    }

    void replaceTrigger(Trigger trigger) {
        synchronized (this.triggerContextMonitor) {
            this.trigger = trigger;
        }
    }

    void replaceDelegate(Runnable delegate) {
        synchronized (this.triggerContextMonitor) {
            if (delegate instanceof EditableRedisLockManager.LockRunnable) {
                ((EditableRedisLockManager.LockRunnable) delegate).task = delegate;
                return;
            }
            this.delegate = delegate;
        }
    }

    @Override
    public void run() {
        Date actualExecutionTime = new Date();
        delegateRun();
        Date completionTime = new Date();
        synchronized (this.triggerContextMonitor) {
            Assert.state(this.scheduledExecutionTime != null, "No scheduled execution");
            this.triggerContext.update(this.scheduledExecutionTime, actualExecutionTime, completionTime);
            if (!obtainCurrentFuture().isCancelled()) {
                schedule();
            }
        }
    }

    public void delegateRun() {
        try {
            this.delegate.run();
        } catch (UndeclaredThrowableException ex) {
            this.errorHandler.handleError(ReflectionUtils.skipReflectionException(ex));
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this.triggerContextMonitor) {
            return obtainCurrentFuture().cancel(mayInterruptIfRunning);
        }
    }

    @Override
    public boolean isCancelled() {
        synchronized (this.triggerContextMonitor) {
            return obtainCurrentFuture().isCancelled();
        }
    }

    @Override
    public boolean isDone() {
        synchronized (this.triggerContextMonitor) {
            return obtainCurrentFuture().isDone();
        }
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        ScheduledFuture<?> curr;
        synchronized (this.triggerContextMonitor) {
            curr = obtainCurrentFuture();
        }
        return curr.get();
    }

    @Override
    public Object get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        ScheduledFuture<?> curr;
        synchronized (this.triggerContextMonitor) {
            curr = obtainCurrentFuture();
        }
        return curr.get(timeout, unit);
    }

    @Override
    public long getDelay(@Nonnull TimeUnit unit) {
        ScheduledFuture<?> curr;
        synchronized (this.triggerContextMonitor) {
            curr = obtainCurrentFuture();
        }
        return curr.getDelay(unit);
    }

    @Override
    public int compareTo(@Nonnull Delayed other) {
        if (this == other) {
            return 0;
        }
        long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
        return (diff == 0 ? 0 : ((diff < 0) ? -1 : 1));
    }

}
