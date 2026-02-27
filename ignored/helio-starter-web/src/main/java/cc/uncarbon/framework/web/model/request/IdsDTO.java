package cc.uncarbon.framework.web.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 用于API接口接收参数
 * 兼容Integer Long String等多种类型的主键
 * 注: 记得加 @RequestBody @Valid 注解
 * @param <T> 主键数据类型
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdsDTO<T extends Serializable> implements Serializable {

    @Schema(description = "主键ID数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "ids不能为空")
    private List<T> ids;

}
