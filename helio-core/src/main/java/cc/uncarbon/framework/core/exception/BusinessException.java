package cc.uncarbon.framework.core.exception;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 业务异常类
 *
 * @author Uncarbon
 */
@NoArgsConstructor
@Getter
public class BusinessException extends RuntimeException {

    private Integer code = null;

    /**
     * 仅错误信息
     * @param msg 错误信息
     */
    public BusinessException(String msg) {
        super(msg);
        this.code = HTTP_INTERNAL_ERROR;
    }

    /**
     * 错误码 + 错误信息
     * @param code 错误码
     * @param msg 错误信息
     */
    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * 错误码 + 错误信息（支持模板参数填充）
     * @param code 错误码
     * @param msg 错误信息
     * @param templateParams 模板参数
     */
    public BusinessException(int code, String msg, Object... templateParams) {
        super(StrUtil.format(msg, templateParams));
        this.code = code;
    }

    /**
     * 从枚举类生成异常
     * @param customEnum 枚举类对象
     */
    public BusinessException(HelioBaseEnum<Integer> customEnum) {
        super(customEnum.getLabel());
        this.code = customEnum.getValue();
    }

    /**
     * 从枚举类生成异常（错误信息支持模板参数填充）
     * @param customEnum 枚举类对象
     * @param templateParams label 中如果有占位符的话，向里面填充的模板参数
     */
    public BusinessException(HelioBaseEnum<Integer> customEnum, Object... templateParams) {
        super(customEnum.formatLabel(templateParams));
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
