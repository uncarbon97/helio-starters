package cc.uncarbon.framework.helium.websecurity.xss;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XSS过滤处理
 *
 * @author Mark@renren-io
 */
@Getter
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * html 过滤器（非线程安全；请求级实例，禁止共享或静态化）
     */
    private final HTMLFilter htmlFilter = new HTMLFilter();

    /**
     * 没被包装过的HttpServletRequest（特殊场景，需要自己过滤）
     */
    HttpServletRequest orgRequest;

    /**
     * 过滤后的 JSON body 缓存（null 表示尚未初始化）
     */
    private byte[] cachedJsonBody;


    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        orgRequest = request;
    }

    /**
     * 工具方法：获取最原始的request
     */
    public static HttpServletRequest getOrgRequest(HttpServletRequest request) {
        if (request instanceof XssHttpServletRequestWrapper xssHttpServletRequestWrapper) {
            return xssHttpServletRequestWrapper.getOrgRequest();
        }
        return request;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // Content-Type为空，直接返回
        String contentType = super.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            return super.getInputStream();
        }

        // 非JSON类型，直接返回
        if (!isJsonContentType(contentType)) {
            return super.getInputStream();
        }

        /*
        JSON body 只过滤一次并缓存：
        - 原始流只能读一次，重复 getInputStream 需返回缓存内容
        - 原始 body 为空白时也缓存（原实现返回已耗尽的原始流，语义错误）
         */
        if (cachedJsonBody == null) {
            String json = IoUtil.read(super.getInputStream(), StandardCharsets.UTF_8);
            if (CharSequenceUtil.isNotBlank(json)) {
                json = xssEncode(json);
            }
            cachedJsonBody = json.getBytes(StandardCharsets.UTF_8);
        }

        final ByteArrayInputStream bis = new ByteArrayInputStream(cachedJsonBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bis.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // do nothing
            }

            @Override
            public int read() {
                return bis.read();
            }
        };
    }

    /**
     * 两级 Content-Type 判断：
     * 一级前缀快速判断，命中即短路；二级 MediaType 解析兜底，识别大小写混写、带空格等变体，防恶意绕过
     */
    private boolean isJsonContentType(String contentType) {
        if (contentType.regionMatches(true, 0, MediaType.APPLICATION_JSON_VALUE, 0,
                MediaType.APPLICATION_JSON_VALUE.length())) {
            return true;
        }
        try {
            return MediaType.APPLICATION_JSON.includes(MediaType.parseMediaType(contentType));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(xssEncode(name));
        if (CharSequenceUtil.isNotBlank(value)) {
            value = xssEncode(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] parameters = super.getParameterValues(name);
        if (parameters == null || parameters.length == 0) {
            return parameters;
        }

        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = xssEncode(parameters[i]);
        }
        return parameters;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        Map<String, String[]> parameters = super.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            for (int i = 0; i < values.length; i++) {
                values[i] = xssEncode(values[i]);
            }
            map.put(key, values);
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(xssEncode(name));
        if (CharSequenceUtil.isNotBlank(value)) {
            value = xssEncode(value);
        }
        return value;
    }

    private String xssEncode(String input) {
        return htmlFilter.filter(input);
    }
}
