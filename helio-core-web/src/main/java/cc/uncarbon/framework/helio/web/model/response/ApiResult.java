package cc.uncarbon.framework.helio.web.model.response;

import cc.uncarbon.framework.helio.base.enums.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResult<T> implements Serializable {

    // 成功时，默认返回的状态码与消息文本
    protected static int DEFAULT_SUCCESS_CODE = 200;
    protected static String DEFAULT_SUCCESS_MSG = "ok";


    @Schema(description = "状态码")
    private Serializable code;

    @Schema(description = "消息文本")
    private String msg;

    @Schema(description = "承载数据")
    private T data;


    public static <T> ApiResult<T> success() {
        return build(DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MSG, null);
    }

    public static <T> ApiResult<T> success(String msg) {
        return build(DEFAULT_SUCCESS_CODE, msg, null);
    }

    public static <T> ApiResult<T> success(T data) {
        return build(DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MSG, data);
    }

    public static <T> ApiResult<T> success(String msg, T data) {
        return build(DEFAULT_SUCCESS_CODE, msg, data);
    }

    public static <T> ApiResult<T> fail(Integer code, String msg) {
        return build(code, msg, null);
    }

    public static <T> ApiResult<T> fail(Integer code, String msg, T data) {
        return build(code, msg, data);
    }

    public static <T> ApiResult<T> fail(String code, String msg) {
        return build(code, msg, null);
    }

    public static <T> ApiResult<T> fail(String code, String msg, T data) {
        return build(code, msg, data);
    }

    public static <T> ApiResult<T> build(BaseEnum<Integer> enumItem) {
        return build(enumItem.getValue(), enumItem.getLabel(), null);
    }

    public static <T> ApiResult<T> build(BaseEnum<Integer> enumItem, T data) {
        return build(enumItem.getValue(), enumItem.getLabel(), data);
    }

    public static <T> ApiResult<T> build(BaseEnum<String> enumItem, String... ignored) {
        return build(enumItem.getValue(), enumItem.getLabel(), null);
    }

    public static <T> ApiResult<T> build(BaseEnum<String> enumItem, T data, String... ignored) {
        return build(enumItem.getValue(), enumItem.getLabel(), data);
    }

    private static <T> ApiResult<T> build(Integer code, String msg, T data) {
        ApiResult<T> ret = new ApiResult<>();
        ret
                .setCode(code)
                .setMsg(msg)
                .setData(data);

        return ret;
    }

    private static <T> ApiResult<T> build(String code, String msg, T data) {
        ApiResult<T> ret = new ApiResult<>();
        ret
                .setCode(code)
                .setMsg(msg)
                .setData(data);

        return ret;
    }
}
