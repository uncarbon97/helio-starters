package cc.uncarbon.framework.helio.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Web 全局异常处理友好消息枚举
 * 枚举值 name 同时视为 i18nCode
 *
 * @author Uncarbon
 */
@AllArgsConstructor
@Getter
public enum GlobalWebExceptionFriendlyMessageEnum {

    NO_LOGIN("需要登录"),
    NO_PERMISSION("无该功能权限"),
    NO_ROLE("无该角色权限"),
    NOT_FOUND_404("你迷路了"),
    NOT_ACCEPTABLE_INPUT("错误的入参格式"),
    METHOD_NOT_ALLOWED("错误的请求方式"),

    /**
     * 一般为最后兜底使用
     */
    INTERNAL_SERVER_ERROR("请稍后再试"),
    ;

    private final String message;

    public String i18nCode() {
        return name();
    }
}
