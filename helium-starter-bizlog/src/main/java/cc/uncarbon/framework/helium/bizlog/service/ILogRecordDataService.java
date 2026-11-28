package cc.uncarbon.framework.helium.bizlog.service;

import cc.uncarbon.framework.helium.bizlog.model.LogRecordModel;


import java.util.List;

/**
 * 日志落库服务
 * <p>
 * 由使用方实现：负责将 {@link LogRecordModel} 持久化，并提供按业务标识的查询能力。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public interface ILogRecordDataService {

    /**
     * 保存一条业务日志。
     *
     * @param model 日志实体
     */
    void record(LogRecordModel model);

    /**
     * 按业务标识 + 业务类型查询日志（返回最多 100 条）。
     *
     * @param bizType 业务类型，比如：订单、商品
     * @param bizNo   操作日志的业务标识，比如：订单号
     * @return 操作日志列表
     */
    List<LogRecordModel> queryLog(String bizNo, String bizType);

    /**
     * 按业务标识 + 业务类型 + 行为查询日志（返回最多 100 条）。
     *
     * @param bizType  业务类型，比如：订单、商品
     * @param behavior 行为，比如：创建订单、修改商品
     * @param bizNo    操作日志的业务标识，比如：订单号
     * @return 操作日志列表
     */
    List<LogRecordModel> queryLogByBizNo(String bizNo, String bizType, String behavior);
}
