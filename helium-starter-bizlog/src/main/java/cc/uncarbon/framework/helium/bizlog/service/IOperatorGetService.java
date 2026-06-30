package cc.uncarbon.framework.helium.bizlog.service;

import cc.uncarbon.framework.helium.bizlog.model.Operator;


/**
 * 操作人获取服务
 * <p>
 * 由使用方实现：从外部（如 SecurityContext / Session / Token）获取操作人并转换为 {@link Operator}。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
public interface IOperatorGetService {

    /**
     * 获取操作人，例如 {@code UserContext.getCurrentUser()}。
     *
     * @return 转换后的操作人
     */
    Operator getOperator();
}
