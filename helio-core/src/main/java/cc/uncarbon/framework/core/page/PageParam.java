package cc.uncarbon.framework.core.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 分页查询参数
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageParam implements Serializable {

    @ApiModelProperty(value = "当前页码")
    private int pageNum = 1;

    @ApiModelProperty(value = "当前页大小")
    private int pageSize = 10;

}
