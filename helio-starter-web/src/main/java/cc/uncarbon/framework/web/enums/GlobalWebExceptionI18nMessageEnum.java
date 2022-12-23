package cc.uncarbon.framework.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Web 全局异常处理国际化消息枚举
 * 枚举值name 同时视为 i18nCode
 *
 * @author Uncarbon
 */
@AllArgsConstructor
@Getter
public enum GlobalWebExceptionI18nMessageEnum {

    GLOBAL__NO_LOGIN("请您先登录"),
    GLOBAL__PERMISSION_NOT_MATCH("您权限不足"),
    GLOBAL__ROLE_NOT_MATCH("您与要求角色不符"),
    GLOBAL__NOT_FOUND("你迷路啦"),
    GLOBAL__UNACCEPTABLE_PARAMETERS("错误参数格式或值"),
    GLOBAL__METHOD_NOT_ALLOWED("错误的请求方式"),

    ;

    private final String defaultValue;

    public String i18nCode() {
        return name();
    }
}
