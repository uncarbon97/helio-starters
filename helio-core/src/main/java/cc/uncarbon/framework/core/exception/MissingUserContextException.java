package cc.uncarbon.framework.core.exception;

import static java.net.HttpURLConnection.HTTP_UNSUPPORTED_TYPE;

import lombok.Getter;

/**
 * 缺少用户上下文异常类
 *
 * @author Uncarbon
 */
@Getter
public class MissingUserContextException extends RuntimeException {

    private final Integer code;


    public MissingUserContextException() {
        super("缺少用户上下文，可能是未登录或用户信息失效");
        this.code = HTTP_UNSUPPORTED_TYPE;
    }
}
