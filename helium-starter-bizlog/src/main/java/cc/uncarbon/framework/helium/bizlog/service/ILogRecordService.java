package cc.uncarbon.framework.helium.bizlog.service;

import cc.uncarbon.framework.helium.bizlog.beans.LogRecord;


import java.util.List;

/**
 * 日志落库服务
 * <p>
 * 由使用方实现：负责将 {@link LogRecord} 持久化（DB / MQ 等），并提供按业务标识的查询能力。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public interface ILogRecordService {
    /**
     * 保存一条业务日志。
     *
     * @param logRecord 日志实体
     */
    void record(LogRecord logRecord);

    /**
     * 按业务标识 + 类型查询日志（返回最多 100 条）。
     *
     * @param type  操作日志类型
     * @param bizNo 操作日志的业务标识，比如：订单号
     * @return 操作日志列表
     */
    List<LogRecord> queryLog(String bizNo, String type);

    /**
     * 按业务标识 + 类型 + 子类型查询日志（返回最多 100 条）。
     *
     * @param type    操作日志类型
     * @param subType 操作日志子类型
     * @param bizNo   操作日志的业务标识，比如：订单号
     * @return 操作日志列表
     */
    List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType);
}
