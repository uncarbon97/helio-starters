package cc.uncarbon.framework.core.page;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果
 * @author Uncarbon
 */
@ApiModel(value = "分页查询结果")
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageResult<T> implements Serializable {

    @ApiModelProperty(value = "当前页")
    private int current;

    @ApiModelProperty(value = "当前页数量")
    private int size;

    @ApiModelProperty(value = "总量")
    private int total;

    @ApiModelProperty(value = "记录")
    private List<T> records;

}
