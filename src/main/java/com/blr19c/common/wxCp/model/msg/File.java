package com.blr19c.common.wxCp.model.msg;


import com.blr19c.common.collection.PictogramMap;

import java.util.Map;


/**
 * 文件消息
 *
 * @author blr
 */
public class File implements Msg, Escape {
    private String mediaId;

    public File(String mediaId) {
        this.mediaId = mediaId;
    }

    public File() {
    }

    @Override
    public Map<String, String> toMap() {
        return PictogramMap.getInstance().putValueNonNull("media_id", mediaId).getMap();
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
}
