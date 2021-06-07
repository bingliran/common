package com.blr19c.common.scheduled;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * EditableScheduled处理器
 *
 * @author blr
 */
public class EditableScheduledAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, EmbeddedValueResolverAware {
    /**
     * Prevent multiple loading
     */
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private final EditableRedisLockManager editableRedisLockManager;
    private StringValueResolver embeddedValueResolver;

    public EditableScheduledAnnotationBeanPostProcessor(EditableRedisLockManager editableRedisLockManager) {
        this.editableRedisLockManager = editableRedisLockManager;
    }

    private static PeriodicTrigger getPeriodicTrigger(long period, long initialDelay, boolean isFixedRate) {
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(period);
        periodicTrigger.setInitialDelay(initialDelay);
        periodicTrigger.setFixedRate(isFixedRate);
        return periodicTrigger;
    }

    private static long parseDelayAsLong(String value) throws RuntimeException {
        if (value.length() > 1 && (isP(value.charAt(0)) || isP(value.charAt(1)))) {
            return Duration.parse(value).toMillis();
        }
        return Long.parseLong(value);
    }

    private static boolean isP(char ch) {
        return (ch == 'P' || ch == 'p');
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) {
        // Ignore AOP infrastructure such as scoped proxies.
        if (bean instanceof AopInfrastructureBean
                || bean instanceof TaskScheduler
                || bean instanceof ScheduledExecutorService) {

            return bean;
        }
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!this.nonAnnotatedClasses.contains(targetClass) &&
                AnnotationUtils.isCandidateClass(targetClass, Arrays.asList(EditableScheduled.class, EditableSchedules.class))) {
            Map<Method, Set<EditableScheduled>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                    (MethodIntrospector.MetadataLookup<Set<EditableScheduled>>) method -> {
                        Set<EditableScheduled> scheduledMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                                method, EditableScheduled.class, EditableSchedules.class);
                        return (!scheduledMethods.isEmpty() ? scheduledMethods : null);
                    });
            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(targetClass);
            } else {
                // Non-empty set of methods
                annotatedMethods.forEach((method, scheduledMethods) ->
                        scheduledMethods.forEach(scheduled -> processScheduled(scheduled, method, bean)));
            }
        }
        return bean;
    }

    @Override
    public void setEmbeddedValueResolver(@Nonnull StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    protected void processScheduled(EditableScheduled scheduled, Method method, Object bean) {
        try {
            // Determine initial delay
            long initialDelay = scheduled.initialDelay();
            String initialDelayString = scheduled.initialDelayString();
            String workId = StringUtils.hasText(scheduled.workId()) ? scheduled.workId() : method.getName();
            Runnable runnable = createRunnable(bean, method, workId, scheduled);
            boolean processedSchedule = false;
            String errorMessage =
                    "Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required";
            if (StringUtils.hasText(initialDelayString)) {
                Assert.isTrue(initialDelay < 0, "Specify 'initialDelay' or 'initialDelayString', not both");
                if (this.embeddedValueResolver != null) {
                    initialDelayString = this.embeddedValueResolver.resolveStringValue(initialDelayString);
                }
                if (StringUtils.hasLength(initialDelayString)) {
                    try {
                        initialDelay = parseDelayAsLong(initialDelayString);
                    } catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid initialDelayString value \"" + initialDelayString + "\" - cannot parse into long");
                    }
                }
            }

            // Check cron expression
            String cron = scheduled.cron();
            if (StringUtils.hasText(cron)) {
                String zone = scheduled.zone();
                if (this.embeddedValueResolver != null) {
                    cron = this.embeddedValueResolver.resolveStringValue(cron);
                    zone = this.embeddedValueResolver.resolveStringValue(zone);
                }
                if (StringUtils.hasLength(cron)) {
                    Assert.isTrue(initialDelay == -1, "'initialDelay' not supported for cron triggers");
                    processedSchedule = true;
                    if (!EditableScheduled.CRON_DISABLED.equals(cron)) {
                        TimeZone timeZone;
                        if (StringUtils.hasText(zone)) {
                            timeZone = StringUtils.parseTimeZoneString(zone);
                        } else {
                            timeZone = TimeZone.getDefault();
                        }
                        ScheduledTaskUtils.addTask(workId, runnable, new CronTrigger(cron, timeZone));
                    }
                }
            }

            // At this point we don't need to differentiate between initial delay set or not anymore
            if (initialDelay < 0) {
                initialDelay = 0;
            }

            // Check fixed delay
            long fixedDelay = scheduled.fixedDelay();
            if (fixedDelay >= 0) {
                Assert.isTrue(!processedSchedule, errorMessage);
                processedSchedule = true;
                ScheduledTaskUtils.addTask(workId, runnable, getPeriodicTrigger(fixedDelay, initialDelay, false));
            }
            String fixedDelayString = scheduled.fixedDelayString();
            if (StringUtils.hasText(fixedDelayString)) {
                if (this.embeddedValueResolver != null) {
                    fixedDelayString = this.embeddedValueResolver.resolveStringValue(fixedDelayString);
                }
                if (StringUtils.hasLength(fixedDelayString)) {
                    Assert.isTrue(!processedSchedule, errorMessage);
                    processedSchedule = true;
                    try {
                        fixedDelay = parseDelayAsLong(fixedDelayString);
                    } catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid fixedDelayString value \"" + fixedDelayString + "\" - cannot parse into long");
                    }
                    ScheduledTaskUtils.addTask(workId, runnable, getPeriodicTrigger(fixedDelay, initialDelay, false));
                }
            }

            // Check fixed rate
            long fixedRate = scheduled.fixedRate();
            if (fixedRate >= 0) {
                Assert.isTrue(!processedSchedule, errorMessage);
                processedSchedule = true;
                ScheduledTaskUtils.addTask(workId, runnable, getPeriodicTrigger(fixedRate, initialDelay, true));
            }
            String fixedRateString = scheduled.fixedRateString();
            if (StringUtils.hasText(fixedRateString)) {
                if (this.embeddedValueResolver != null) {
                    fixedRateString = this.embeddedValueResolver.resolveStringValue(fixedRateString);
                }
                if (StringUtils.hasLength(fixedRateString)) {
                    Assert.isTrue(!processedSchedule, errorMessage);
                    processedSchedule = true;
                    try {
                        fixedRate = parseDelayAsLong(fixedRateString);
                    } catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid fixedRateString value \"" + fixedRateString + "\" - cannot parse into long");
                    }
                    ScheduledTaskUtils.addTask(workId, runnable, getPeriodicTrigger(fixedRate, initialDelay, true));
                }
            }

            // Check whether we had any attribute set
            Assert.isTrue(processedSchedule, errorMessage);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            throw new IllegalStateException(
                    "Encountered invalid @EditableScheduled method '" + method.getName() + "': " + ex.getMessage());
        }
    }

    private Runnable createRunnable(Object target, Method method, String workId, EditableScheduled scheduled) {
        Assert.isTrue(method.getParameterCount() == 0, "Only no-arg methods may be annotated with @EditableScheduled");
        Method invocableMethod = AopUtils.selectInvocableMethod(method, target.getClass());
        Runnable runnable = new ScheduledMethodRunnable(target, invocableMethod);
        if (scheduled.enableSchedulerLock())
            return editableRedisLockManager.getLockRunnable(runnable, workId, scheduled);
        return runnable;
    }
}
