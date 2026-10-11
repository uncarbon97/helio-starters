package cc.uncarbon.framework.helium.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ReUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * IP地址工具类
 * 方法来自于互联网
 * <p>
 * 注意：本工具采信的代理头（X-Forwarded-For 等）均可由客户端伪造；
 * 用于审计/风控等信任敏感场景时，需由前置代理强制覆写或清洗这些头。
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
     * <p>代理头（如 X-Forwarded-For）可能是逗号拼接的多级代理链，取首个合法 IP，
     * 避免整串文本直达下游；全部不合法时回退 remoteAddr</p>
     * @param servletRequest 请求对象
     * @return 客户端IP地址
     */
    public String getClientIPAddress(HttpServletRequest servletRequest) {
        String firstValidIp = firstValidIp(resolveRawClientIp(servletRequest));
        return firstValidIp != null ? firstValidIp : servletRequest.getRemoteAddr();
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
        String source = resolveRawClientIp(servletRequest);
        if (!CharSequenceUtil.contains(source, StrPool.COMMA)) {
            // 不含逗号
            return List.of(source);
        }
        return CharSequenceUtil.split(source, StrPool.COMMA).stream()
                // 过滤空串，避免 clientIpList 含 "" 元素
                .filter(CharSequenceUtil::isNotBlank).map(CharSequenceUtil::cleanBlank).toList();
    }

    /**
     * 按代理头优先级取原始值（可能是逗号拼接的代理链，或 remoteAddr）
     */
    private String resolveRawClientIp(HttpServletRequest servletRequest) {
        for (String header : HEADERS_TO_TRY) {
            String ip = servletRequest.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return servletRequest.getRemoteAddr();
    }

    /**
     * 从（可能是逗号拼接的）代理链中取首个合法 IP，无合法值返回 null
     */
    private String firstValidIp(String candidateChain) {
        if (!CharSequenceUtil.contains(candidateChain, StrPool.COMMA)) {
            String trimmed = CharSequenceUtil.trim(candidateChain);
            return isValidIp(trimmed) ? trimmed : null;
        }
        for (String candidate : CharSequenceUtil.split(candidateChain, StrPool.COMMA)) {
            String trimmed = CharSequenceUtil.trim(candidate);
            if (isValidIp(trimmed)) {
                return trimmed;
            }
        }
        return null;
    }

    /**
     * 含冒号视为 IPv6 字面量；其余（域名等伪造值）不采信
     */
    private boolean isValidIp(String ip) {
        return isIPv4(ip) || CharSequenceUtil.contains(ip, StrPool.COLON);
    }

    private boolean isIPv4(String ip) {
        return ReUtil.isMatch(PatternPool.IPV4, ip);
    }
}
