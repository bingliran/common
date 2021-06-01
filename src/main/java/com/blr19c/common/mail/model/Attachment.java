package com.blr19c.common.mail.model;

import javax.activation.DataSource;
import java.io.File;
import java.io.InputStream;

/**
 * 附件
 */
public class Attachment {

    /**
     * 文件名称
     */
    protected String fileName;

    /**
     * 文件流 当使用此种流格式时必须指定 contentType
     */
    protected InputStream inputStream;

    /**
     * 资源文件 此种方式不需要额外指定contentType
     */
    protected DataSource dataSource;

    /**
     * 文件,不需要指定contentType 也可以不指定fileName
     */
    protected File file;

    /**
     * 内容类型
     */
    protected String contentType;

    public Attachment(String fileName, InputStream inputStream, String contentType) {
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.contentType = contentType;
    }

    public Attachment(String fileName, DataSource dataSource) {
        this.fileName = fileName;
        this.dataSource = dataSource;
    }

    public Attachment(File file) {
        this.file = file;
        this.fileName = file.getName();
    }

    public Attachment() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
