package com.blr19c.common.wxCp.model;


import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.wxCp.config.WxCp;
import com.blr19c.common.wxCp.enums.MsgTypeEnum;
import com.blr19c.common.wxCp.model.msg.Escape;
import com.blr19c.common.wxCp.model.msg.Msg;

import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;

/**
 * 请求体
 *
 * @author blr
 */
public class RequestStyle {
    /**
     * 发送至--->
     */
    private List<String> toUser;
    /**
     * 发送至部门--->
     */
    private List<String> toParty;
    /**
     * 发送至标签--->
     */
    private List<String> toTag;
    /**
     * 消息类型
     */
    private MsgTypeEnum msgType;
    /**
     * 程序id
     */
    private String agentId;
    /**
     * 消息
     */
    private Msg msg;

    public RequestStyle(List<String> toUser, List<String> toParty, List<String> toTag, MsgTypeEnum msgType, String agentId, Msg msg) {
        this.toUser = toUser;
        this.toParty = toParty;
        this.toTag = toTag;
        this.msgType = msgType;
        this.agentId = agentId;
        this.msg = msg;
    }

    public RequestStyle() {
    }

    public List<String> getToUser() {
        return toUser;
    }

    public void setToUser(List<String> toUser) {
        this.toUser = toUser;
    }

    public List<String> getToParty() {
        return toParty;
    }

    public void setToParty(List<String> toParty) {
        this.toParty = toParty;
    }

    public List<String> getToTag() {
        return toTag;
    }

    public void setToTag(List<String> toTag) {
        this.toTag = toTag;
    }

    public MsgTypeEnum getMsgType() {
        return msgType;
    }

    public void setMsgType(MsgTypeEnum msgType) {
        this.msgType = msgType;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Msg getMsg() {
        return msg;
    }

    public void setMsg(Msg msg) {
        this.msg = msg;
    }

    /**
     * 生成request的map
     */
    public PictogramMap getRequest(WxCp wxCp) {
        return PictogramMap.toPictogramMapAsModel(this, (entry) -> {
            Object value = null;
            if (entry.getValue() instanceof List) value = join((Iterable<?>) entry.getValue());
            if (entry.getValue() instanceof MsgTypeEnum) value = ((MsgTypeEnum) entry.getValue()).getMsgType();
            if (entry.getValue() instanceof Escape) value = ((Escape) entry.getValue()).toMap();
            if (Objects.isNull(value)) value = entry.getValue();
            return new AbstractMap.SimpleEntry<>(String.valueOf(entry.getKey()).toLowerCase(), value);
        })
                .putValue("enable_duplicate_check", wxCp.getEnableDuplicateCheck())
                .putValue("duplicate_check_interval", wxCp.getDuplicateCheckInterval())
                .putValue("enable_id_trans", wxCp.getEnableIdTrans())
                .putValueToThis(msgType.getMsgType(), "msg");
    }

    @SuppressWarnings("unchecked")
    private String join(Iterable<?> elements) {
        if (Objects.isNull(elements)) return "";
        return String.join("|", (Iterable<? extends CharSequence>) elements);
    }
}
