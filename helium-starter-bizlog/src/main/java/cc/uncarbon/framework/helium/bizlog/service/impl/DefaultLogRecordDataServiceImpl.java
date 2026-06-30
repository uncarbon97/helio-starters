package cc.uncarbon.framework.helium.bizlog.service.impl;

import cc.uncarbon.framework.helium.bizlog.model.LogRecordModel;
import cc.uncarbon.framework.helium.bizlog.service.ILogRecordDataService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 日志落库服务默认实现
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Slf4j
public class DefaultLogRecordDataServiceImpl implements ILogRecordDataService {

    /**
     * 默认仅将日志打印到控制台
     */
    @Override
    public void record(LogRecordModel model) {
        log.info("业务日志: {}", model);
    }

    /**
     * 默认实现返回空列表
     */
    @Override
    public List<LogRecordModel> queryLog(String bizNo, String type) {
        return List.of();
    }

    /**
     * 默认实现返回空列表
     */
    @Override
    public List<LogRecordModel> queryLogByBizNo(String bizNo, String type, String subType) {
        return List.of();
    }
}
