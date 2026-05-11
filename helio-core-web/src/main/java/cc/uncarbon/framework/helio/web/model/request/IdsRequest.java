package cc.uncarbon.framework.helio.web.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 用于 API 接口接收参数
 * 兼容 Integer / Long / String 等多种类型的主键
 * 使用时，还需要手动加上 @RequestBody @Valid 注解
 *
 * @param <T> 主键数据类型
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdsRequest<T extends Serializable> implements Serializable {

    @Schema(description = "主键ID数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "ids必填")
    private List<T> ids;

}
