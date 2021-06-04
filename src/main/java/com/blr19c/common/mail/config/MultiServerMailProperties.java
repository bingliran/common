package com.blr19c.common.mail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Arrays;

@ConfigurationProperties(prefix = "spring.multi-server-mail")
public class MultiServerMailProperties {

    @NestedConfigurationProperty
    private MailProperties[] mail;

    private boolean enable;

    public MailProperties[] getMail() {
        return mail;
    }

    public void setMail(MailProperties[] mail) {
        this.mail = mail;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "MultiServerMailConfig{" +
                "mail=" + Arrays.toString(mail) +
                '}';
    }
}
