package com.blr19c.common.wxCp.enums;


import com.blr19c.common.wxCp.model.msg.*;

import java.util.Arrays;

/**
 * 消息类型枚举
 *
 * @author blr
 */
public enum MsgTypeEnum {
    TEXT("text", "文本消息", Text.class),
    TEXT_CARD("textcard", "文本卡片", TextCard.class),
    MARK_DOWN("markdown", "md文本卡片", Markdown.class),
    IMAGE("image", "图片", Image.class),
    VOICE("voice", "语音", Voice.class),
    VIDEO("video", "视频", Video.class),
    FILE("file", "普通文件", File.class);

    private final String msgType;
    private final String description;
    private final Class<? extends Msg> clsType;

    MsgTypeEnum(String msgType, String description, Class<? extends Msg> clsType) {
        this.msgType = msgType;
        this.clsType = clsType;
        this.description = description;
    }

    public static MsgTypeEnum value(MediaTypeEnum mediaTypeEnum) {
        return Arrays.stream(values()).filter(msgTypeEnum -> msgTypeEnum.getMsgType().equals(mediaTypeEnum.getType())).findAny().orElse(null);
    }

    public String getMsgType() {
        return msgType;
    }

    public String getDescription() {
        return description;
    }

    public Class<? extends Msg> getClsType() {
        return clsType;
    }
}
