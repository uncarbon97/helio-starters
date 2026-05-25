package cc.uncarbon.framework.helium.base.page;

/**
 * 标记某个查询入参类，同时用于分页查询
 *
 * @author Uncarbon
 */
public interface PageQuery {

    /**
     * 取得分页查询参数
     */
    PageParam getPagination();

    /**
     * 捷径 - 取当前页码
     */
    default Integer getPageNum() {
        if (getPagination() == null) {
            return null;
        }
        return getPagination().getPageNum();
    }

    /**
     * 捷径 - 取当前页大小
     */
    default Integer getPageSize() {
        if (getPagination() == null) {
            return null;
        }
        return getPagination().getPageSize();
    }
}
