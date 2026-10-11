package cc.uncarbon.framework.helium.base.exception;

/**
 * 值解析失败异常
 *
 * @author Uncarbon
 */
public class ValueParseException extends RuntimeException {

    public ValueParseException(String message) {
        super(message);
    }

    /**
     * 关闭爬栈（入参错误，无需溯源）
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
