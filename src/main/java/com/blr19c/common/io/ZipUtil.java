package com.blr19c.common.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * zip工具
 *
 * @author blr
 */
public class ZipUtil {

    /**
     * 转换为zip
     *
     * @param zips   压缩数据
     * @param method 压缩等级
     */
    public static byte[] toZip(List<Zip> zips, int method) throws IOException {
        ByteArrayOutputStream fn;
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(fn = new ByteArrayOutputStream())) {
            zipOutputStream.setMethod(method);
            for (Zip zip : zips) {
                ZipEntry zipEntry = new ZipEntry(zip.name);
                zipEntry.setSize(zip.data.length);
                CRC32 crc = new CRC32();
                crc.update(zip.data);
                zipEntry.setCrc(crc.getValue());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(zip.data);
            }
            zipOutputStream.finish();
        }
        return fn.toByteArray();
    }

    public static class Zip {

        /**
         * 文件名称
         * 可以用{@link File#separator}分割作为前文件夹
         */
        String name;
        /**
         * 数据
         */
        byte[] data;

        public Zip(String name, byte[] data) {
            this.name = name;
            this.data = data;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }
}
