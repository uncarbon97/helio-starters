package cc.uncarbon.framework.helium.bizlog.service.impl;


import cc.uncarbon.framework.helium.bizlog.beans.Operator;
import cc.uncarbon.framework.helium.bizlog.service.IOperatorGetService;

/**
 * 操作人获取服务默认实现
 * <p>
 * 默认返回占位操作人 {@code "111"}；生产环境应自行实现 {@link IOperatorGetService}，
 * 从 SecurityContext / Session / Token 等处注入真实用户。
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
    public Operator getUser() {
        // return Optional.ofNullable(UserUtils.getUser())
        //                .map(a -> new Operator(a.getName(), a.getLogin()))
        //                .orElseThrow(()->new IllegalArgumentException("user is null"));
        Operator operator = new Operator();
        operator.setOperatorId("111");
        return operator;
    }
}
