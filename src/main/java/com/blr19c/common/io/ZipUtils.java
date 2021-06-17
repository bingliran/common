package com.blr19c.common.io;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * zip工具
 *
 * @author blr
 */
public class ZipUtils {

    /**
     * 转换为zip
     *
     * @param zips   压缩数据
     * @param method 压缩等级
     */
    public static byte[] toZip(List<Zip> zips, int method) throws IOException {
        ByteOutputStream fn = new ByteOutputStream();
        toZip(zips, method, fn);
        return fn.getBytes();
    }

    /**
     * 转换为zip
     *
     * @param zips   压缩数据
     * @param method 压缩等级
     * @param out    目标
     */
    public static void toZip(List<Zip> zips, int method, OutputStream out) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new CheckedOutputStream(out, new CRC32()))) {
            zipOutputStream.setMethod(method);
            for (Zip zip : zips) {
                ZipEntry zipEntry = new ZipEntry(zip.name);
                InputStream resource = zip.resource;
                zipEntry.setSize(resource.available());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(IOUtils.toByteArray(resource));
                zip.close();
            }
            zipOutputStream.finish();
        }
    }


    public static class Zip implements Closeable {

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

    public static class ByteZip extends Zip {

        public ByteZip(String name, byte[] data) {
            super(name, new ByteArrayInputStream(data));
        }
    }
}
