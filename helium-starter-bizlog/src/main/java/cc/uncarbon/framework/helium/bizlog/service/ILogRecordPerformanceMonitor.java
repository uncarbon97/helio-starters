package cc.uncarbon.framework.helium.bizlog.service;

import org.springframework.util.StopWatch;

/**
 * 日志记录性能监控器
 * <p>
 * 用于输出日志拦截各阶段的耗时；默认实现仅打印，可自行实现接入监控平台。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public interface ILogRecordPerformanceMonitor {

    /**
     * 性能监控任务名前缀
     */
    String MONITOR_NAME = "log-record-performance";

    /**
     * 前置执行阶段任务名
     */
    String MONITOR_TASK_BEFORE_EXECUTE = "before-execute";

    /**
     * 后置执行阶段任务名
     */
    String MONITOR_TASK_AFTER_EXECUTE = "after-execute";

    /**
     * 输出性能计时结果。
     *
     * @param stopWatch 计时器
     */
    void print(StopWatch stopWatch);
}
