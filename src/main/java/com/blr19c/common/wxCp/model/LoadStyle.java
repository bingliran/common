package com.blr19c.common.wxCp.model;

import com.blr19c.common.wxCp.enums.MediaTypeEnum;

import java.util.Date;

/**
 * 素材上传返回体
 *
 * @author blr
 */
public class LoadStyle extends ResponseStyle {
    /**
     * 媒体类型
     */
    private MediaTypeEnum type;
    /**
     * 素材id
     */
    private String mediaId;
    /**
     * 创建时间
     */
    private Date createdAt;

    public LoadStyle(MediaTypeEnum type, String mediaId, Date createdAt) {
        this.type = type;
        this.mediaId = mediaId;
        this.createdAt = createdAt;
    }

    public LoadStyle() {
    }

    public MediaTypeEnum getType() {
        return type;
    }

    public void setType(MediaTypeEnum type) {
        this.type = type;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
