package cc.uncarbon.framework.satoken.util;

import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 * 方法来自于互联网
 * @author Uncarbon
 * @deprecated since 1.8.0，改为使用helio-starter-web包中的同名工具类
 */
@Deprecated
@UtilityClass
public class IPUtil {
    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR",
            "X-Real-IP"};

    /***
     * 获取客户端IP地址(可以穿透代理)
     * @param request 请求对象
     * @return IP地址
     */
    public String getClientIPAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

}
