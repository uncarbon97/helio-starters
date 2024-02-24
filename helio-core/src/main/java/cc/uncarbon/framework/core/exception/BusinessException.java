package cc.uncarbon.framework.core.exception;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpStatus;
import lombok.Getter;
import lombok.NonNull;

/**
 * 业务异常类
 *
 * @author Uncarbon
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    /*
    若使用枚举类参数的构造方法创建的本异常，则记录对应枚举类及模板参数
     */
    private final Enum<?> customEnumField;
    private final Object[] templateParams;


    /**
     * 仅错误信息
     *
     * @param msg 错误信息
     */
    public BusinessException(String msg) {
        super(msg);
        this.code = HttpStatus.HTTP_INTERNAL_ERROR;
        this.customEnumField = null;
        this.templateParams = null;
    }

    /**
     * 错误码 + 错误信息
     *
     * @param code 错误码
     * @param msg  错误信息
     */
    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.customEnumField = null;
        this.templateParams = null;
    }

    /**
     * 错误码 + 错误信息（支持模板参数填充）
     *
     * @param code            错误码
     * @param msg             错误信息
     * @param templateParams  模板参数
     */
    public BusinessException(int code, String msg, Object... templateParams) {
        super(CharSequenceUtil.format(msg, templateParams));
        this.code = code;
        this.customEnumField = null;
        this.templateParams = templateParams;
    }

    /**
     * 从枚举类生成异常
     *
     * @param customEnum 枚举类对象
     */
    public BusinessException(@NonNull HelioBaseEnum<?> customEnum) {
        super(customEnum.getLabel());
        this.code = customEnum.getValueAsInt();

        /*
        @since 1.7.2 国际化支持
        实际应用在GlobalWebExceptionHandlerAutoConfiguration.handleBusinessException
         */
        if (customEnum.getClass().isEnum()) {
            this.customEnumField = (Enum<?>) customEnum;
        } else {
            this.customEnumField = null;
        }
        this.templateParams = null;
    }

    /**
     * 从枚举类生成异常（错误信息支持模板参数填充）
     *
     * @param customEnum      枚举类对象
     * @param templateParams  label 中如果有占位符的话，向里面填充的模板参数
     */
    public BusinessException(@NonNull HelioBaseEnum<?> customEnum, Object... templateParams) {
        super(customEnum.formatLabel(templateParams));
        this.code = customEnum.getValueAsInt();

        /*
        @since 1.7.2 国际化支持
        实际应用在GlobalWebExceptionHandlerAutoConfiguration.handleBusinessException
         */
        if (customEnum.getClass().isEnum()) {
            this.customEnumField = (Enum<?>) customEnum;
        } else {
            this.customEnumField = null;
        }
        this.templateParams = templateParams;
    }

    /**
     * 关闭爬栈
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
