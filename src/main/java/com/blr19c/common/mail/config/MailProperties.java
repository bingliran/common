package com.blr19c.common.mail.config;

import java.util.List;

public class MailProperties extends org.springframework.boot.autoconfigure.mail.MailProperties {

    /**
     * 指定的后缀 使用此服务器发送 例如 - @qq.com
     */
    private List<String> suffixMatching;

    /**
     * 此服务器名称
     */
    private String componentName;

    /**
     * 只能有一个的 MailConfig 的 primary 为true
     * 如果没有任何suffixMatching匹配发送的邮箱账号则使用为true的发送
     * 如果没有任何为true的则{@link IllegalArgumentException}
     */
    private boolean primary;

    public List<String> getSuffixMatching() {
        return suffixMatching;
    }

    public void setSuffixMatching(List<String> suffixMatching) {
        this.suffixMatching = suffixMatching;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        return "MailConfig{" +
                "host='" + getHost() + '\'' +
                ", port=" + getPort() +
                ", username='" + getUsername() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", protocol='" + getProtocol() + '\'' +
                ", defaultEncoding=" + getDefaultEncoding() +
                ", properties=" + getProperties() +
                ", jndiName='" + getJndiName() + '\'' +
                ", suffixMatching=" + getSuffixMatching() +
                ", componentName='" + getComponentName() + '\'' +
                "} ";
    }
}
