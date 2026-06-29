package cc.uncarbon.framework.helium.bizlog.service.impl;

import cc.uncarbon.framework.helium.bizlog.service.ILogRecordPerformanceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

/**
 * 日志记录性能监控器默认实现
 * <p>
 * 以 DEBUG 级别打印 {@link StopWatch} 计时详情。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Slf4j
public class DefaultLogRecordPerformanceMonitor implements ILogRecordPerformanceMonitor {

    /**
     * 输出性能计时结果。
     *
     * @param stopWatch 计时器
     */
    @Override
    public void print(StopWatch stopWatch) {
        log.debug("LogRecord performance={}", stopWatch.prettyPrint());
    }
}
