package cc.uncarbon.framework.core.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * Helio脚手架内部出错异常类
 *
 * @author Uncarbon
 */
@Slf4j
public class HelioFrameworkException extends RuntimeException {

    public HelioFrameworkException(String msg) {
        super(msg);
        log.error("Helio framework has internal exception: " + msg);
    }
}
