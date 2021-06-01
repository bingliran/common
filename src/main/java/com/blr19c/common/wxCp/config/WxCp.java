package com.blr19c.common.wxCp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 请求项
 *
 * @author blr
 */
@ConfigurationProperties(prefix = "wxcp")
public class WxCp {
    /**
     * 公司id
     */
    private String corpId;
    /**
     * 程序Secret
     */
    private String corpSecret;
    /**
     * 程序id
     */
    private String agentId;
    /**
     * 信息url
     */
    private String sendUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send";
    /**
     * 验证url
     */
    private String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    /**
     * 材料上传url
     */
    private String loadUrl = "https://qyapi.weixin.qq.com/cgi-bin/media/upload";
    /**
     * 是否开始重复校验 1:开启 0:关闭
     */
    private int enableDuplicateCheck;
    /**
     * 重复信息不接收时间(默认1800) 单位:秒
     */
    private int duplicateCheckInterval;
    /**
     * 是否为保密消息 0:否 1:是
     */
    private int safe;
    /**
     * 是否开启id转译 0:否 1:是
     */
    private int enableIdTrans;
    /**
     * 是否启用
     */
    private boolean enable;

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getCorpSecret() {
        return corpSecret;
    }

    public void setCorpSecret(String corpSecret) {
        this.corpSecret = corpSecret;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getSendUrl() {
        return sendUrl;
    }

    public void setSendUrl(String sendUrl) {
        this.sendUrl = sendUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getLoadUrl() {
        return loadUrl;
    }

    public void setLoadUrl(String loadUrl) {
        this.loadUrl = loadUrl;
    }

    public int getEnableDuplicateCheck() {
        return enableDuplicateCheck;
    }

    public void setEnableDuplicateCheck(int enableDuplicateCheck) {
        this.enableDuplicateCheck = enableDuplicateCheck;
    }

    public int getDuplicateCheckInterval() {
        return duplicateCheckInterval;
    }

    public void setDuplicateCheckInterval(int duplicateCheckInterval) {
        this.duplicateCheckInterval = duplicateCheckInterval;
    }

    public int getSafe() {
        return safe;
    }

    public void setSafe(int safe) {
        this.safe = safe;
    }

    public int getEnableIdTrans() {
        return enableIdTrans;
    }

    public void setEnableIdTrans(int enableIdTrans) {
        this.enableIdTrans = enableIdTrans;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
