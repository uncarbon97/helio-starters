package cc.uncarbon.framework.web.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
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
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdsDTO<T> implements Serializable {

    @ApiModelProperty(value = "主键ID数组", required = true)
    @NotEmpty(message = "ids不能为空")
    private List<T> ids;

}
