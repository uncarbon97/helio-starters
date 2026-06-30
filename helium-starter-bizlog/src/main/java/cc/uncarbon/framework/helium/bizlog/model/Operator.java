package cc.uncarbon.framework.helium.bizlog.model;

import cc.uncarbon.framework.helium.bizlog.service.IOperatorGetService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 操作人
 * <p>
 * 由 {@link IOperatorGetService} 解析得到，用于填充日志的操作人字段。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Operator {

    /**
     * 操作人ID
     */
    private String operatorId;

}
