package cc.uncarbon.framework.core.exception;

import static java.net.HttpURLConnection.HTTP_UNSUPPORTED_TYPE;

import lombok.Getter;

/**
 * 缺少租户上下文异常类
 *
 * @author Uncarbon
 */
@Getter
public class MissingTenantContextException extends RuntimeException {

    private final Integer code;


    public MissingTenantContextException() {
        super("缺少租户上下文，可能是租户信息配置错误");
        this.code = HTTP_UNSUPPORTED_TYPE;
    }
}
