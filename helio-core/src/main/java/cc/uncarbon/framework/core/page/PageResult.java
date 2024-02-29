package cc.uncarbon.framework.core.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果
 * @param <T> records字段的泛型类型
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageResult<T extends Serializable> implements Serializable {

    @Schema(description = "当前页")
    private long current;

    @Schema(description = "当前页数量")
    private long size;

    @Schema(description = "总量")
    private long total;

    @Schema(description = "记录")
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
