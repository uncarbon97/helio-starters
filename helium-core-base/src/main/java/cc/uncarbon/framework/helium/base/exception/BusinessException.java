package cc.uncarbon.framework.helium.base.exception;

import cc.uncarbon.framework.helium.base.errorcode.ErrorCodeEnum;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * 业务异常类
 *
 * @author Uncarbon
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * 如果异常由 {@link ErrorCodeEnum} 创建，则携带原始枚举
     */
    private final ErrorCodeEnum errorCodeEnum;

    /**
     * 如果异常由 {@link ErrorCodeEnum} 创建，则携带原始变量填充
     */
    private final Object[] templateParams;


    /**
     * 错误码 + 错误信息
     *
     * @param code     错误码
     * @param errorMsg 错误信息
     */
    public BusinessException(@NonNull String code, @NonNull String errorMsg) {
        super(errorMsg);
        this.code = code;
        this.errorCodeEnum = null;
        this.templateParams = null;
    }

    /**
     * 错误码 + 错误信息，支持字符串模板填充参数
     *
     * @param code             错误码
     * @param errorMsgTemplate 错误信息模板，支持使用 {} 作为变量填充符
     * @param templateParams   填充错误信息模板的参数
     */
    public BusinessException(@NonNull String code, @NonNull String errorMsgTemplate, Object... templateParams) {
        super(CharSequenceUtil.format(errorMsgTemplate, templateParams));
        this.code = code;
        this.errorCodeEnum = null;
        this.templateParams = null;
    }

    /**
     * 错误码 + 错误信息，支持字符串模板填充参数
     *
     * @param errorCodeEnum  错误码枚举
     * @param templateParams 填充错误信息模板的参数
     */
    public BusinessException(@NonNull ErrorCodeEnum errorCodeEnum, Object... templateParams) {
        super(CharSequenceUtil.format(errorCodeEnum.getErrorMsgFriendly(), templateParams));
        this.code = errorCodeEnum.getErrorCode();
        this.errorCodeEnum = errorCodeEnum;
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
