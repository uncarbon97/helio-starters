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
 * <p>getter 只读：钳制结果不回写字段，{@link #rawPageNum()} / {@link #rawPageSize()} 始终返回调用方传入的原值，
 * 便于审计「用户传入 99999 被钳到 500」这类信息；序列化 / 日志多次读取不会改变对象状态</p>
 *
 * @author Uncarbon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PageParam implements Serializable {

    /**
     * 默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 普通分页页大小上限；与分页拦截器 maxLimit 共用同一口径
     */
    public static final int MAX_PAGE_SIZE = 500;

    @Schema(description = "当前页码")
    protected Integer pageNum;

    @Schema(description = "当前页大小")
    protected Integer pageSize;

    /**
     * 全局分页页码限制器；volatile 保证启动期由自动配置赋值后对其他线程可见
     */
    private static volatile UnaryOperator<Integer> globalPageNumLimiter = null;

    /**
     * 全局分页大小限制器（如：限制页大小上限为1000）
     */
    private static volatile UnaryOperator<Integer> globalPageSizeLimiter = null;

    /**
     * 指定全局分页页码限制器
     */
    public static void setGlobalPageNumLimiter(UnaryOperator<Integer> globalPageNumLimiter) {
        PageParam.globalPageNumLimiter = globalPageNumLimiter;
    }

    /**
     * 指定全局分页大小限制器
     */
    public static void setGlobalPageSizeLimiter(UnaryOperator<Integer> globalPageSizeLimiter) {
        PageParam.globalPageSizeLimiter = globalPageSizeLimiter;
    }

    /**
     * 取得 pageNum 原始值（未钳制）
     */
    public Integer rawPageNum() {
        return pageNum;
    }

    /**
     * 页码只做下限钳制（>=1），不做上限：深分页性能由使用方自行防护（如游标分页），属有意特性
     */
    public Integer getPageNum() {
        int num = pageNum == null ? 1 : Math.max(pageNum, 1);
        return applyGlobalPageNumLimiter(num);
    }

    /**
     * 取得 pageSize 原始值（未钳制）
     */
    public Integer rawPageSize() {
        return pageSize;
    }

    public Integer getPageSize() {
        int size = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        // 钳制到 [1, MAX_PAGE_SIZE]，避免 0/负值触发全表查询
        size = Math.clamp(size, 1, MAX_PAGE_SIZE);
        return applyGlobalPageSizeLimiter(size);
    }

    /*
    ----------------------------------------------------------------
                        受保护静态工具方法
    ----------------------------------------------------------------
     */

    /**
     * 应用全局分页页码限制器；供 getPageNum 复用，也供子类覆写后复用，避免绕过全局策略
     */
    protected static Integer applyGlobalPageNumLimiter(Integer num) {
        UnaryOperator<Integer> limiter = globalPageNumLimiter;
        return limiter != null ? limiter.apply(num) : num;
    }

    /**
     * 应用全局分页大小限制器；供 getPageSize 及子类覆写后复用，避免绕过全局策略
     */
    protected static Integer applyGlobalPageSizeLimiter(Integer size) {
        UnaryOperator<Integer> limiter = globalPageSizeLimiter;
        return limiter != null ? limiter.apply(size) : size;
    }
}
