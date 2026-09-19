package cc.uncarbon.framework.helium.base.page;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 大数据分页查询参数
 *
 * @author Uncarbon
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@Data
public class LargePageParam extends PageParam {

    /**
     * 默认页大小
     */
    private static final int DEFAULT_PAGE_SIZE = 10000;


    public LargePageParam(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    @Override
    public Integer getPageSize() {
        Integer pageSize = rawPageSize();
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }
}
