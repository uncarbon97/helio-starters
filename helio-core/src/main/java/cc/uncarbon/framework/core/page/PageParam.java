package cc.uncarbon.framework.core.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.function.UnaryOperator;

/**
 * 分页查询参数
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageParam implements Serializable {

    @ApiModelProperty(value = "当前页码")
    private Integer pageNum;
    public Integer getPageNum() {
        if (pageNum == null) {
            pageNum = 1;
        }

        if (globalPageNumLimiter != null) {
            pageNum = globalPageNumLimiter.apply(pageNum);
        }
        return pageNum;
    }

    @ApiModelProperty(value = "当前页大小")
    private Integer pageSize;
    public Integer getPageSize() {
        if (pageSize == null) {
            pageSize = 10;
        }

        if (globalPageSizeLimiter != null) {
            return globalPageSizeLimiter.apply(pageSize);
        }
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
}
