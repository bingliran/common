package com.blr19c.common.wxCp.config;

import com.blr19c.common.wxCp.WxCpUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "wxcp.enable", havingValue = "true")
@EnableConfigurationProperties(WxCp.class)
public class WxCpAutoConfig {

    public WxCpAutoConfig(WxCp wxCp) {
        WxCpUtils.setWxCp(wxCp);
    }
}
