package cc.uncarbon.framework.helium.bizlog.service.impl;

import cc.uncarbon.framework.helium.bizlog.beans.LogRecord;
import cc.uncarbon.framework.helium.bizlog.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志落库服务默认实现
 * <p>
 * 默认仅将日志打印到控制台；生产环境应自行实现 {@link ILogRecordService} 落库（如写入 DB / MQ）。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Slf4j
public class DefaultLogRecordServiceImpl implements ILogRecordService {

//    @Resource
//    private LogRecordMapper logRecordMapper;

    /**
     * 记录一条业务日志。
     *
     * @param logRecord 业务日志
     */
    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(LogRecord logRecord) {
        log.info("【logRecord】log={}", logRecord);
        //throw new RuntimeException("sss");
//        logRecordMapper.insertSelective(logRecord);
    }

    /**
     * 按业务标识 + 类型查询日志（默认实现返回空列表）。
     *
     * @param bizNo 业务标识
     * @param type  业务类型
     * @return 日志列表
     */
    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        return new ArrayList<>();
    }

    /**
     * 按业务标识 + 类型 + 子类型查询日志（默认实现返回空列表）。
     *
     * @param bizNo   业务标识
     * @param type    业务类型
     * @param subType 业务子类型
     * @return 日志列表
     */
    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        return new ArrayList<>();
    }


}
