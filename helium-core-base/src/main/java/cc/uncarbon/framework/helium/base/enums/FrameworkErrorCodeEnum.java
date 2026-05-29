package cc.uncarbon.framework.helium.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 框架内置错误码枚举类
 */
@AllArgsConstructor
@Getter
public enum FrameworkErrorCodeEnum implements ErrorCodeEnum {

    /**
     * 操作成功
     */
    OK("OK", "ok"),

    /*
    错误码格式 [A][BB][CCC]
    [A] 固定为 Z，表示框架内置错误
    [BB] 固定为 00
    [CCC] 按具体错误区分
     */

    // ref HTTP_BAD_REQUEST
    Z00400("Z00400", "{}"),
    // ref HTTP_UNAUTHORIZED
    Z00401("Z00401", "需要重新登录"),
    Z00402("Z00402", "无该角色权限"),
    // ref HTTP_FORBIDDEN
    Z00403("Z00403", "无该功能权限"),
    // ref HTTP_NOT_FOUND
    Z00404("Z00404", "你迷路了"),
    // ref HTTP_METHOD_NOT_ALLOWED
    Z00405("Z00405", "错误的请求方式"),
    // ref HTTP_NOT_ACCEPTABLE
    Z00406("Z00406", "错误的入参格式"),
    // ref HTTP_TOO_MANY_REQUESTS
    Z00429("Z00429", "操作频率不要太快，稍微休息一下吧"),
    Z00500("Z00500", "服务器繁忙，请稍后再试"),

    ;private final String errorCode;
    private final String errorMsgFriendly;

}
