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
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LargePageParam extends PageParam {

    /**
     * 默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10000;

    /**
     * 页大小上限；与分页拦截器 maxLimit 共用同一口径
     */
    public static final int MAX_PAGE_SIZE = 10000;


    public LargePageParam(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    @Override
    public Integer getPageSize() {
        Integer raw = rawPageSize();
        int size = raw == null ? DEFAULT_PAGE_SIZE : raw;
        // 钳制到 [1, 10000]，避免 0/负值触发全表查询；再复用全局页大小限制器，避免绕过全局策略
        size = Math.clamp(size, 1, MAX_PAGE_SIZE);
        return applyGlobalPageSizeLimiter(size);
    }
}
