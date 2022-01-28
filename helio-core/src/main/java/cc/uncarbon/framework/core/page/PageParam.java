package cc.uncarbon.framework.core.page;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 分页查询参数
 * @author Uncarbon
 */
@ApiModel(value = "分页查询参数")
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageParam implements Serializable {

    @ApiModelProperty(value = "当前页码")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "当前页大小")
    private Integer pageSize = 10;

}
