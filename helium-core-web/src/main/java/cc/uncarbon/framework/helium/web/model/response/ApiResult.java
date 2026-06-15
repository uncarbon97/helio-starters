package cc.uncarbon.framework.helium.web.model.response;

import cc.uncarbon.framework.helium.base.errorcode.BuiltinErrorCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * HTTP API 返回对象包装
 *
 * @param <T> 承载数据类型
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class ApiResult<T> implements Serializable {


    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "错误码")
    private String code;

    @Schema(description = "消息文本")
    private String msg;

    @Schema(description = "承载数据")
    private T data;


    public static <T> ApiResult<T> success() {
        final var ok = BuiltinErrorCodeEnum.OK;
        return new ApiResult<>(true, ok.getErrorCode(), ok.getErrorMsgFriendly(), null);
    }

    public static <T> ApiResult<T> success(String msg) {
        final var ok = BuiltinErrorCodeEnum.OK;
        return new ApiResult<>(true, ok.getErrorCode(), msg, null);
    }

    public static <T> ApiResult<T> success(T data) {
        final var ok = BuiltinErrorCodeEnum.OK;
        return new ApiResult<>(true, ok.getErrorCode(), ok.getErrorMsgFriendly(), data);
    }

    public static <T> ApiResult<T> success(String msg, T data) {
        final var ok = BuiltinErrorCodeEnum.OK;
        return new ApiResult<>(true, ok.getErrorCode(), msg, data);
    }

    public static <T> ApiResult<T> fail(String code, String msg) {
        return new ApiResult<>(false, code, msg, null);
    }

    public static <T> ApiResult<T> fail(String code, String msg, T data) {
        return new ApiResult<>(false, code, msg, data);
    }
}
