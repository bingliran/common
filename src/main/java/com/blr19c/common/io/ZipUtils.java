package com.blr19c.common.io;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        ByteArrayOutputStream fn = new ByteArrayOutputStream();
        toZip(zips, method, fn);
        return fn.toByteArray();
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

}
