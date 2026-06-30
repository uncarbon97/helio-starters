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
     * 按业务标识 + 类型查询日志（返回最多 100 条）。
     *
     * @param type  操作日志类型
     * @param bizNo 操作日志的业务标识，比如：订单号
     * @return 操作日志列表
     */
    List<LogRecordModel> queryLog(String bizNo, String type);

    /**
     * 按业务标识 + 类型 + 子类型查询日志（返回最多 100 条）。
     *
     * @param type    操作日志类型
     * @param subType 操作日志子类型
     * @param bizNo   操作日志的业务标识，比如：订单号
     * @return 操作日志列表
     */
    List<LogRecordModel> queryLogByBizNo(String bizNo, String type, String subType);
}
