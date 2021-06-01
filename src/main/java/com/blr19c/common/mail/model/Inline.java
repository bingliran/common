package com.blr19c.common.mail.model;

import javax.activation.DataSource;
import java.io.File;
import java.io.InputStream;

/**
 * 图片
 */
public class Inline extends Attachment {

    /**
     * html内联图片id
     */
    private String contentId;

    public Inline(String fileName, InputStream inputStream, String contentType, String contentId) {
        super(fileName, inputStream, contentType);
        this.contentId = contentId;
    }

    public Inline(String fileName, DataSource dataSource, String contentId) {
        super(fileName, dataSource);
        this.contentId = contentId;
    }

    public Inline(File file, String contentId) {
        super(file);
        this.contentId = contentId;
    }

    public Inline() {
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
}
