package com.blr19c.common.mybatisWrapper;

import com.blr19c.common.mybatisWrapper.wrapper.SqlSessionTemplateWrapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MybatisWrapperAutoConfig {

    @Bean
    @ConditionalOnBean(SqlSessionFactory.class)
    @Primary
    public SqlSessionTemplateWrapper sqlSessionTemplateWrapper(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplateWrapper(sqlSessionFactory);
    }
}
