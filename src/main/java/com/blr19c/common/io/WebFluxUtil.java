package com.blr19c.common.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * 使用WebFlux请求
 *
 * @author blr
 */
public class WebFluxUtil {

    /**
     * @param url        请求url
     * @param httpMethod 请求方式
     * @param data       请求数据
     * @param returnType 返回值类型  T代表返回的类型
     * @param doError    出现网络等异常时的处理方法(非返回异常码)
     * @param headers    请求头
     * @param cookies    请求cookie
     * @param <T>        数据类型
     * @throws HttpLaunchException 未经处理的异常会以HttpLaunchException的形式表现
     */
    public static <T> T launch(String url, HttpMethod httpMethod, Object data, Class<T> returnType, Consumer<? super Throwable> doError, Map<String, List<String>> headers, Map<String, List<String>> cookies) {
        if (cookies == null) cookies = new HashMap<>();
        if (headers == null) headers = new HashMap<>();
        if (data == null) data = new HashMap<>();
        if (doError == null) doError = throwable -> {
            throw new HttpLaunchException(throwable);
        };
        List<String> contentType = headers.isEmpty() ? Collections.singletonList(APPLICATION_JSON_VALUE) : headers.get(CONTENT_TYPE);
        final Map<String, List<String>> finalHeaders = headers;
        final Map<String, List<String>> finalCookies = cookies;
        return WebClient.builder()
                .build()
                .method(httpMethod).uri(url)
                .headers(h -> h.putAll(finalHeaders))
                .cookies(c -> c.putAll(finalCookies))
                .header(CONTENT_TYPE, contentType.get(0))
                .body(getBody(data))
                .retrieve()
                .bodyToMono(returnType)
                .doOnError(doError)
                .block();
    }

    /**
     * 更简单的上传文件接口
     * (使用launch上传文件实现更复杂)
     *
     * @param url        请求url
     * @param name       文件标识名
     * @param fileName   文件名
     * @param data       数据
     * @param returnType 返回类型
     * @throws IOException          网络异常
     * @throws NullPointerException 参数以及返回异常
     */
    public static <T> T upFileCall(String url, String name, String fileName, byte[] data, Class<T> returnType) throws IOException {
        ResponseBody body = new OkHttpClient()
                .newCall(new Request.Builder()
                        .url(url)
                        .post(new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addPart(MultipartBody.Part.createFormData(name, fileName, RequestBody.create(MediaType.parse("multipart/form-data"), data)))
                                .build())
                        .build())
                .execute().body();
        if (body == null)
            throw new HttpLaunchException("body is null");
        return new ObjectMapper().readValue(body.string(), returnType);
    }

    @SuppressWarnings("unchecked")
    private static BodyInserter<?, ? super ClientHttpRequest> getBody(Object data) {
        if (data instanceof MultiValueMap)
            return fromMultipartData((MultiValueMap<String, ?>) data);
        return fromValue(data);
    }

    public static <T> T launch(String url, HttpMethod httpMethod, Object data, Class<T> returnType, Consumer<? super Throwable> doError, Map<String, List<String>> headers) {
        return launch(url, httpMethod, data, returnType, doError, headers, null);
    }

    public static <T> T launch(String url, HttpMethod httpMethod, Object data, Class<T> returnType, Consumer<? super Throwable> doError) {
        return launch(url, httpMethod, data, returnType, doError, null);
    }

    public static <T> T launch(String url, HttpMethod httpMethod, Object data, Class<T> returnType) {
        return launch(url, httpMethod, data, returnType, null);
    }

    public static <T> T getLaunch(String url, Object data, Class<T> returnType, Consumer<? super Throwable> doError) {
        return launch(url, HttpMethod.GET, data, returnType, doError);
    }

    public static <T> T getLaunch(String url, Object data, Class<T> returnType) {
        return getLaunch(url, data, returnType, null);
    }

    public static <T> T postLaunch(String url, Object data, Class<T> returnType, Consumer<? super Throwable> doError, Map<String, List<String>> headers) {
        return launch(url, HttpMethod.POST, data, returnType, doError, headers);
    }

    public static <T> T postLaunch(String url, Object data, Class<T> returnType, Consumer<? super Throwable> doError) {
        return postLaunch(url, data, returnType, doError, null);
    }

    public static <T> T postLaunch(String url, Object data, Class<T> returnType) {
        return postLaunch(url, data, returnType, null);
    }

    public static class HttpLaunchException extends RuntimeException {
        public HttpLaunchException(Throwable throwable) {
            super(throwable);
        }

        public HttpLaunchException(String message) {
            super(message);
        }
    }
}