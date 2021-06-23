package com.blr19c.common.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 代表一个zip文件
 *
 * @author blr
 */
public class Zip implements Closeable {

    /**
     * 文件名称
     * 可以用{@link File#separator}分割作为前文件夹
     */
    protected String name;
    /**
     * 文件资源
     */
    protected InputStream resource;

    public Zip(String name, InputStream resource) {
        this.name = name;
        this.resource = resource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputStream getResource() {
        return resource;
    }

    public void setResource(InputStream resource) {
        this.resource = resource;
    }

    @Override
    public void close() throws IOException {
        if (resource != null)
            resource.close();
    }
}