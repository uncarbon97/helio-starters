package cc.uncarbon.framework.helium.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;

/**
 * IP地址工具类
 * 方法来自于互联网
 *
 * @author Uncarbon
 */
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
     * @param servletRequest 请求对象
     * @return 客户端IP地址
     */
    public String getClientIPAddress(HttpServletRequest servletRequest) {
        for (String header : HEADERS_TO_TRY) {
            String ip = servletRequest.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return servletRequest.getRemoteAddr();
    }

    /***
     * 获取客户端IP地址(可以穿透代理)
     * @param servletRequest 请求对象
     * @param indexOfCommaSplit 先按逗号分隔后，再取第index个IP地址（从0开始）；兼容启用了云防护盾CDN的服务器（可能获取到的IP会带上中间代理节点的IP地址）
     * @return 客户端IP地址
     */
    public String getClientIPAddress(HttpServletRequest servletRequest, int indexOfCommaSplit) {
        List<String> addressList = getClientIPAddressList(servletRequest);
        return CollUtil.get(addressList, indexOfCommaSplit);
    }

    /***
     * 获取客户端IP地址集合(可以穿透代理)
     * @param servletRequest 请求对象
     * @return 客户端IP地址
     */
    public List<String> getClientIPAddressList(HttpServletRequest servletRequest) {
        String source = getClientIPAddress(servletRequest);
        if (!CharSequenceUtil.contains(source, StrPool.COMMA)) {
            // 不含逗号
            return List.of(source);
        }
        return CharSequenceUtil.split(source, StrPool.COMMA).stream()
                .map(CharSequenceUtil::cleanBlank).filter(Objects::nonNull).toList();
    }

}
