package cc.uncarbon.framework.helium.base.page;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * 标记某个查询入参类，同时用于分页查询
 *
 * @author Uncarbon
 */
public interface PageQuery {

    /**
     * 取得分页查询参数
     */
    PageParam getPageParam();

    /**
     * 捷径 - 取当前页码
     */
    @Hidden
    default Integer getPageNum() {
        if (getPageParam() == null) {
            return null;
        }
        return getPageParam().getPageNum();
    }

    /**
     * 捷径 - 取当前页大小
     */
    @Hidden
    default Integer getPageSize() {
        if (getPageParam() == null) {
            return null;
        }
        return getPageParam().getPageSize();
    }
}
