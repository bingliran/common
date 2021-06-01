package com.blr19c.common.scheduled;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnBean(StringRedisTemplate.class)
public class ScheduledAutoConfig {

    @Bean
    public EditableRedisLockManager editableRedisLockManager(StringRedisTemplate stringRedisTemplate) {
        return new EditableRedisLockManager(stringRedisTemplate);
    }

    @Bean
    public EditableScheduledAnnotationBeanPostProcessor editableScheduledAnnotationBeanPostProcessor(StringRedisTemplate stringRedisTemplate) {
        return new EditableScheduledAnnotationBeanPostProcessor(editableRedisLockManager(stringRedisTemplate));
    }
}
