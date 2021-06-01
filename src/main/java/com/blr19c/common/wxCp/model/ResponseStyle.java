package com.blr19c.common.wxCp.model;

import org.apache.commons.lang3.StringUtils;

/**
 * 返回体
 *
 * @author blr
 */
public class ResponseStyle {
    /**
     * 失败状态码 成功则为空
     */
    private String errCode;
    /**
     * 失败原因 成功则为ok
     */
    private String errMsg;
    /**
     * 未发送给(发送失败)的user 多个则为 user1|user2
     */
    private String invalidUser;
    /**
     * 未发送给的部门 同user格式
     */
    private String invalidParty;
    /**
     * 未发送给的标签 同user格式
     */
    private String invalidTag;

    public ResponseStyle(String errCode, String errMsg, String invalidUser, String invalidParty, String invalidTag) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.invalidUser = invalidUser;
        this.invalidParty = invalidParty;
        this.invalidTag = invalidTag;
    }

    public ResponseStyle() {
    }

    /**
     * 是否发送成功
     */
    public boolean isSuccess() {
        return StringUtils.isBlank(errCode) || "0".equals(errCode);
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getInvalidUser() {
        return invalidUser;
    }

    public void setInvalidUser(String invalidUser) {
        this.invalidUser = invalidUser;
    }

    public String getInvalidParty() {
        return invalidParty;
    }

    public void setInvalidParty(String invalidParty) {
        this.invalidParty = invalidParty;
    }

    public String getInvalidTag() {
        return invalidTag;
    }

    public void setInvalidTag(String invalidTag) {
        this.invalidTag = invalidTag;
    }
}
