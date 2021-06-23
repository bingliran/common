package com.blr19c.common.io;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * 关于ip的工具类
 *
 * @author blr
 */
public class IPUtils {

    public static String getIp(HttpServletRequest request) {
        Assert.notNull(request, "request not be null");
        String ip = request.getHeader("x-forwarded-for");
        if (invalidIp(ip))
            ip = request.getHeader("Proxy-Client-IP");
        if (invalidIp(ip))
            ip = request.getHeader("X-Forwarded-For");
        if (invalidIp(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");
        if (invalidIp(ip))
            ip = request.getHeader("X-Real-IP");
        if (invalidIp(ip))
            ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip.split(",")[0];
    }

    private static boolean invalidIp(String ip) {
        return !StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip);
    }

    /**
     * 完整请求路径
     */
    public static String getUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder();
        url.append(request.getScheme()).append("://").append(request.getServerName());
        url.append(":").append(request.getServerPort()).append(request.getRequestURI());
        if (request.getQueryString() != null) url.append("?").append(request.getQueryString());
        return url.toString();
    }

    /**
     * 根据ip获取粗略地址
     */
    public static AddressInfo getAddress(String ip) throws GeoIp2Exception {
        try (DatabaseReader reader = new DatabaseReader.Builder(ResourceUtils.getFile("classpath:address/GeoLite2-City.mmdb")).build()) {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            return new AddressInfo(response);
        } catch (GeoIp2Exception e) {
            throw e;
        } catch (Exception e) {
            throw new GeoIp2Exception("unknown", e);
        }
    }
}
