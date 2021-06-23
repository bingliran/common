package com.blr19c.common.io;


import java.io.ByteArrayInputStream;

/**
 * 字节zip文件
 *
 * @author blr
 */
public class ByteZip extends Zip {

    public ByteZip(String name, byte[] data) {
        super(name, new ByteArrayInputStream(data));
    }
}