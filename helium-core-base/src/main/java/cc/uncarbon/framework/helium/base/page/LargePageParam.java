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

    /**
     * 页大小上限
     */
    private static final int MAX_PAGE_SIZE = 10000;


    public LargePageParam(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    @Override
    public Integer getPageSize() {
        Integer pageSize = rawPageSize();
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        // 钳制到 [1, 10000]，避免 0/负值在 maxLimit=-1 时触发全表查询
        return Math.clamp(pageSize, 1, MAX_PAGE_SIZE);
    }
}
