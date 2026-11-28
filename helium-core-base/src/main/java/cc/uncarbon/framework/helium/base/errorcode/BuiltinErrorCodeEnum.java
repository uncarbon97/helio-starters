package cc.uncarbon.framework.helium.base.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 框架内置结构化错误码
 * [A] 固定为 Z
 * [BB] 固定为 00
 * [CCC] 具体错误代号
 */
@Getter
@AllArgsConstructor
public enum BuiltinErrorCodeEnum implements StructuredErrorCode {

    /**
     * 操作成功
     */
    OK("ok"),

    // ref HTTP_BAD_REQUEST
    Z00400("{}"),
    // ref HTTP_UNAUTHORIZED
    Z00401("需要重新登录"),
    Z00402("无该角色权限"),
    // ref HTTP_FORBIDDEN
    Z00403("无该功能权限"),
    // ref HTTP_NOT_FOUND
    Z00404("你迷路了"),
    // ref HTTP_METHOD_NOT_ALLOWED
    Z00405("错误的请求方式"),
    // ref HTTP_NOT_ACCEPTABLE
    Z00406("错误的入参格式"),
    // ref HTTP_CONTENT_TOO_LARGE
    Z00413("文件大小超限"),
    // ref HTTP_UNSUPPORTED_MEDIA_TYPE
    Z00415("不支持的媒体类型"),
    // ref HTTP_TOO_MANY_REQUESTS
    Z00429("操作频率不要太快，稍微休息一下吧"),
    Z00500("服务器繁忙，请稍后再试或联系管理员"),

    ;
    private final String errorMsgFriendly;

}
