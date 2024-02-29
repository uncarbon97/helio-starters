package cc.uncarbon.framework.web.model.response;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * HTTP接口通用返回对象
 * @param <T> 承载数据类型
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResult<T> implements Serializable {

    @Schema(description = "状态码")
    private int code;

    @Schema(description = "返回消息")
    private String msg;

    @Schema(description = "承载数据")
    private T data;

    public static <T> ApiResult<T> success() {
        return build(HttpStatus.HTTP_OK, HelioConstant.Message.SUCCESS, null);
    }

    public static <T> ApiResult<T> success(String msg) {
        return build(HttpStatus.HTTP_OK, msg, null);
    }

    public static <T> ApiResult<T> fail(Integer code, String msg) {
        return build(code, msg, null);
    }

    public static <T> ApiResult<T> fail(Integer code, String msg, T data) {
        return build(code, msg, data);
    }

    public static <T> ApiResult<T> data(T data) {
        return build(HttpStatus.HTTP_OK,
                ObjectUtil.isEmpty(data) ? HelioConstant.Message.NULL : HelioConstant.Message.SUCCESS,
                data);
    }

    public static <T> ApiResult<T> data(String msg, T data) {
        return build(HttpStatus.HTTP_OK, msg, data);
    }

    public static <T> ApiResult<T> build(HelioBaseEnum<Integer> enumItem) {
        return build(enumItem.getValue(), enumItem.getLabel(), null);
    }

    public static <T> ApiResult<T> build(HelioBaseEnum<Integer> enumItem, T data) {
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
}
