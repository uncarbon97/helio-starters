package cc.uncarbon.framework.helium.base.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.function.UnaryOperator;

/**
 * 普通分页查询参数
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageParam implements Serializable {

    @Schema(description = "当前页码")
    protected Integer pageNum;

    @Schema(description = "当前页大小")
    protected Integer pageSize;

    /**
     * 应用全局分页大小限制器；供子类覆写 getPageSize 后复用，避免绕过全局策略
     */
    protected static Integer applyGlobalPageSizeLimiter(Integer size) {
        if (globalPageSizeLimiter != null) {
            return globalPageSizeLimiter.apply(size);
        }
        return size;
    }

    /**
     * 页码只做下限钳制（>=1），不做上限：深分页性能由使用方自行防护（如游标分页），属有意特性
     */
    public Integer getPageNum() {
        if (pageNum == null) {
            pageNum = 1;
        }

        pageNum = Math.max(pageNum, 1);

        if (globalPageNumLimiter != null) {
            pageNum = globalPageNumLimiter.apply(pageNum);
        }
        return pageNum;
    }

    /**
     * 取得 pageSize 原始值
     */
    public Integer rawPageSize() {
        return pageSize;
    }

    /**
     * 全局分页页码限制器
     */
    private static UnaryOperator<Integer> globalPageNumLimiter = null;

    /**
     * 指定全局分页页码限制器
     */
    public static synchronized void setGlobalPageNumLimiter(UnaryOperator<Integer> globalPageNumLimiter) {
        PageParam.globalPageNumLimiter = globalPageNumLimiter;
    }

    /**
     * 全局分页大小限制器（如：限制页大小上限为1000）
     */
    private static UnaryOperator<Integer> globalPageSizeLimiter = null;

    /**
     * 指定全局分页大小限制器
     */
    public static synchronized void setGlobalPageSizeLimiter(UnaryOperator<Integer> globalPageSizeLimiter) {
        PageParam.globalPageSizeLimiter = globalPageSizeLimiter;
    }

    public Integer getPageSize() {
        if (pageSize == null) {
            pageSize = 10;
        }

        // 钳制到 [1, 500]，避免 0/负值在 maxLimit=-1 时触发全表查询
        pageSize = Math.clamp(pageSize, 1, 500);

        return applyGlobalPageSizeLimiter(pageSize);
    }
}
