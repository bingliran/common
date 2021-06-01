package com.blr19c.common.io;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 返回结果工具
 */
public class ResponseUtil {

    /**
     * springboot 返回文件
     */
    public static ResponseEntity<InputStreamResource> autoDownLoad(String attachmentName,
                                                                   long available,
                                                                   InputStream inputStream) {
        try {
            HttpHeaders headers = new HttpHeaders();
            String attachment = URLEncoder.encode(attachmentName, "UTF-8");
            headers.setContentDispositionFormData("attachment", attachment);
            headers.setContentLength(available);
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
