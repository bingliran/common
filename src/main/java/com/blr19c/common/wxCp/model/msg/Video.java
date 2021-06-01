package com.blr19c.common.wxCp.model.msg;


import com.blr19c.common.collection.PictogramMap;

import java.util.Map;

/**
 * 视频消息
 *
 * @author blr
 */
public class Video extends File {
    private String title;
    private String description;

    public Video(String mediaId, String title, String description) {
        super(mediaId);
        this.title = title;
        this.description = description;
    }

    public Video() {
    }

    @Override
    public Map<String, String> toMap() {
        return PictogramMap.toPictogramMap(super.toMap())
                .putValue("title", title)
                .putValue("description", description).getMap();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
