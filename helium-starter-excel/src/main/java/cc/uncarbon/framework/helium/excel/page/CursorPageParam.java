package cc.uncarbon.framework.helium.excel.page;

import cc.uncarbon.framework.helium.base.page.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 游标分页查询参数，支持以主键游标续批，避免偏移翻页在表数据量增减时的漂移，与深分页开销
 *
 * @author Uncarbon
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CursorPageParam extends PageParam {

    /**
     * 默认页大小
     */
    public static final int PAGE_SIZE_DEFAULT = 1000;

    /**
     * 页大小上限
     */
    public static final int PAGE_SIZE_MAX = PageParam.LARGE_PAGE_SIZE_MAX;

    /**
     * 游标：上一批末条记录主键；null = 首批
     */
    private Long lastId;

    public CursorPageParam(Integer pageNum, Integer pageSize, Long lastId) {
        super(pageNum, pageSize);
        this.lastId = lastId;
    }

    @Override
    public Integer getPageSize() {
        Integer raw = rawPageSize();
        int size = raw == null ? PAGE_SIZE_DEFAULT : raw;
        // 钳制值域，避免 0/负值触发全表查询
        size = Math.clamp(size, 1, PAGE_SIZE_MAX);
        return applyGlobalPageSizeLimiter(size);
    }
}
