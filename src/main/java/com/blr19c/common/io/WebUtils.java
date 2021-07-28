package com.blr19c.common.io;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 返回结果工具
 */
public class WebUtils {

    /**
     * 获取当前请求绑定信息
     */
    public static ServletRequestAttributes servletRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 获取request
     */
    public static HttpServletRequest request() {
        return servletRequestAttributes().getRequest();
    }

    /**
     * 获取response
     */
    public static HttpServletResponse response() {
        return servletRequestAttributes().getResponse();
    }

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
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
