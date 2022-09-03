package cc.uncarbon.framework.core.page;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Collections;
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
public class PageResult<T extends Serializable> implements Serializable {

    @ApiModelProperty(value = "当前页")
    private long current;

    @ApiModelProperty(value = "当前页数量")
    private long size;

    @ApiModelProperty(value = "总量")
    private long total;

    @ApiModelProperty(value = "记录")
    private List<T> records;


    /**
     * 构造方法 - 用于空集合结果
     * @param pageParam 分页查询参数
     */
    public PageResult(PageParam pageParam) {
        this.current = pageParam.getPageNum();
        this.size = pageParam.getPageSize();
        this.total = 0L;
        this.records = Collections.emptyList();
    }

}
