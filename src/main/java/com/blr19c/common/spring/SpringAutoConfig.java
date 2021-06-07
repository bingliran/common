package com.blr19c.common.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAutoConfig {

    @Bean
    public SpringBeanUtils springBeanUtil() {
        return new SpringBeanUtils();
    }
}
