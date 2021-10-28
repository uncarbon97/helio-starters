package cc.uncarbon.framework.core.exception;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 业务异常类
 * @author Uncarbon
 */
@NoArgsConstructor
@Getter
public class BusinessException extends RuntimeException {

    private Integer code = null;

    public BusinessException(String msg) {
        super(msg);
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(HelioBaseEnum<Integer> customEnum) {
        super(customEnum.getLabel());
        this.code = customEnum.getValue();
    }

    /**
     * 关闭爬栈
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
