package cc.uncarbon.framework.helium.web.exception;

/**
 * 异常归类
 *
 * @author Uncarbon
 */
public enum ExceptionCategory {

    /**
     * 主动抛出的业务异常
     */
    BUSINESS,

    /**
     * 有预期的异常（参数不合法、未登录、无权限、404 等框架已归类处理的异常）
     */
    EXPECTED,

    /**
     * 兜底未归类异常（SQL、RPC、RuntimeException 等，通常需要重点监控）
     */
    UNEXPECTED
}
