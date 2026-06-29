package cc.uncarbon.framework.helium.bizlog.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作人
 * <p>
 * 由 {@code IOperatorGetService} 解析得到，用于填充日志的操作人字段。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operator {
    /**
     * 操作人ID
     */
    private String operatorId;
}
