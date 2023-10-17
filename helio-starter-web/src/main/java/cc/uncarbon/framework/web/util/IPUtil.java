package cc.uncarbon.framework.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * IP地址工具类
 * 方法来自于互联网
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
     * @param request 请求对象
     * @return 客户端IP地址
     */
    public String getClientIPAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /***
     * 获取客户端IP地址(可以穿透代理)
     * @param request 请求对象
     * @param indexOfCommaSplit 先按逗号分隔后，再取第index个IP地址（从0开始）；兼容启用了云防护盾CDN的服务器（可能获取到的IP会带上中间代理节点的IP地址）
     * @return 客户端IP地址
     */
    public String getClientIPAddress(HttpServletRequest request, int indexOfCommaSplit) {
        String source = getClientIPAddress(request);
        if (!StrUtil.contains(source, StrPool.COMMA)) {
            // 不含逗号
            return source;
        }
        List<String> split = StrUtil.split(source, StrPool.COMMA);
        return StrUtil.cleanBlank(CollUtil.get(split, indexOfCommaSplit));
    }

}
