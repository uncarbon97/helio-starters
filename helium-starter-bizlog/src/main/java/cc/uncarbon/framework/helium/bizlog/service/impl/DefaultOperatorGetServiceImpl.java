package cc.uncarbon.framework.helium.bizlog.service.impl;


import cc.uncarbon.framework.helium.bizlog.model.Operator;
import cc.uncarbon.framework.helium.bizlog.service.IOperatorGetService;

/**
 * 操作人获取服务默认实现
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
public class DefaultOperatorGetServiceImpl implements IOperatorGetService {

    /**
     * 获取当前操作人。
     *
     * @return 占位操作人（ID 为 {@code "111"}）
     */
    @Override
    public Operator getOperator() {
        return new Operator().setOperatorId(getClass().getSimpleName());
    }
}
