package cc.uncarbon.framework.helium.web.exception;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 异常处理钩子上下文
 *
 * @param exception      异常实例
 * @param servletRequest 当前请求
 * @param category       异常归类
 * @author Uncarbon
 */
public record ExceptionHookContext(
        Exception exception,
        HttpServletRequest servletRequest,
        ExceptionCategory category) {
}
