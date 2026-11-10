package cc.uncarbon.framework.helium.websecurity.xss;

import java.io.IOException;

/**
 * XSS 过滤需缓冲的请求体超过上限
 *
 * <p>继承 {@link IOException}：在 {@code getInputStream()} 调用链上抛出，由 {@link XssFilter} 捕获并响应 413</p>
 *
 * @author Uncarbon
 */
public class RequestBodyTooLargeException extends IOException {

    private final long actualBytes;
    private final long maxBytes;

    public RequestBodyTooLargeException(long actualBytes, long maxBytes) {
        super("请求体超过 XSS 过滤缓冲上限: actual>=" + actualBytes + "B, max=" + maxBytes + "B");
        this.actualBytes = actualBytes;
        this.maxBytes = maxBytes;
    }

    public long getActualBytes() {
        return actualBytes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }
}
