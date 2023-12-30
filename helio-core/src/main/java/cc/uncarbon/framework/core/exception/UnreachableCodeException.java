package cc.uncarbon.framework.core.exception;

/**
 * 不可达代码异常类
 *
 * @author Uncarbon
 */
public class UnreachableCodeException extends RuntimeException {

    public UnreachableCodeException() {
        super("Execution path unreachable");
    }
}
