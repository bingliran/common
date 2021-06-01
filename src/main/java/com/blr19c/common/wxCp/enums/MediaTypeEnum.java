package com.blr19c.common.wxCp.enums;

/**
 * 媒体类型枚举
 *
 * @author blr
 */
public enum MediaTypeEnum {
    /**
     * 所有文件size必须大于5个字节
     * <p>
     * 图片（image）：2MB，支持JPG,PNG格式
     * 语音（voice） ：2MB，播放长度不超过60s，仅支持AMR格式
     * 视频（video） ：10MB，支持MP4格式
     * 普通文件（file）：20MB
     */

    IMAGE("image", "图片", 2),
    VOICE("voice", "语音", 2),
    VIDEO("video", "视频", 10),
    FILE("file", "普通文件", 20);
    private final String type;
    private final String description;
    private final int maxMB;

    MediaTypeEnum(String type, String description, int maxMB) {
        this.type = type;
        this.description = description;
        this.maxMB = maxMB;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxMB() {
        return maxMB;
    }
}
