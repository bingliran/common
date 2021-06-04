package com.blr19c.common.mail.config;

import com.blr19c.common.mail.MailUtil;
import com.blr19c.common.mail.MultipleMailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.multi-server-mail.enable", havingValue = "true")
@EnableConfigurationProperties(MultiServerMailProperties.class)
public class MailAutoConfig {

    @Bean
    public MultipleMailSender multipleMailSender(MultiServerMailProperties properties) {
        MailProperties[] mailProperties = properties.getMail();
        MultipleMailSender multipleMailSender = new MultipleMailSender();
        //组合JavaMailSender
        for (MailProperties mailProperty : mailProperties) {
            multipleMailSender.addJavaMailSender(mailProperty);
        }
        MailUtil.setMultipleMailSender(multipleMailSender);
        return multipleMailSender;
    }
}
