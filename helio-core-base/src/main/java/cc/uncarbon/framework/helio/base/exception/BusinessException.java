package cc.uncarbon.framework.helio.base.exception;

import cn.hutool.core.text.CharSequenceUtil;
import com.sun.source.tree.InstanceOfTree;
import lombok.Getter;

import java.io.Serializable;

/**
 * 业务异常类
 *
 * @author Uncarbon
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 默认错误码
     */
    public static final int DEFAULT_ERROR_CODE = 500;

    /**
     * 错误码，同时兼容整数与文本
     */
    private final Serializable code;


    /**
     * 仅错误信息
     *
     * @param errorMsg 错误信息
     */
    public BusinessException(String errorMsg) {
        super(errorMsg);
        this.code = DEFAULT_ERROR_CODE;
    }

    /**
     * 错误码 + 错误信息
     *
     * @param code     错误码
     * @param errorMsg 错误信息
     */
    public BusinessException(int code, String errorMsg) {
        super(errorMsg);
        this.code = code;
    }

    /**
     * 错误码 + 错误信息
     *
     * @param code     错误码
     * @param errorMsg 错误信息
     */
    public BusinessException(String code, String errorMsg) {
        super(errorMsg);
        this.code = code;
    }

    /**
     * 错误码 + 错误信息，支持字符串模板填充参数
     *
     * @param code             错误码
     * @param errorMsgTemplate 错误信息模板，支持使用 {} 作为占位符
     * @param templateParams   填充错误信息模板的参数
     */
    public BusinessException(int code, String errorMsgTemplate, Object... templateParams) {
        super(CharSequenceUtil.format(errorMsgTemplate, templateParams));
        this.code = code;
    }

    /**
     * 错误码 + 错误信息，支持字符串模板填充参数
     *
     * @param code             错误码
     * @param errorMsgTemplate 错误信息模板，支持使用 {} 作为占位符
     * @param templateParams   填充错误信息模板的参数
     */
    public BusinessException(String code, String errorMsgTemplate, Object... templateParams) {
        super(CharSequenceUtil.format(errorMsgTemplate, templateParams));
        this.code = code;
    }

    /**
     * 关闭爬栈
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    /**
     * @return 整数形式的错误码；如果实际不是整数类型，也会返回 null
     */
    public Integer getCodeAsInt() {
        Serializable code = getCode();
        if (code == null) return null;
        if (code instanceof Integer codeAsInt) {
            return codeAsInt;
        }
        return null;
    }

    /**
     * @return 文本形式的错误码；如果实际不是整数类型，会尝试强行转换成文本
     */
    public String getCodeAsString() {
        Serializable code = getCode();
        if (code == null) return null;
        if (code instanceof String codeAsString) {
            return codeAsString;
        }
        return code.toString();
    }
}
