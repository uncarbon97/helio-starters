package cc.uncarbon.framework.redis.support;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis分布式可重入锁辅助类
 *
 * @author Uncarbon
 */
public interface RedisDistributedLockSupport {

    /**
     * 【注意】务必考虑到锁内代码有抛出异常的可能
     * 编程式获取、释放分布式锁
     * 规范化分布式锁的使用方式，减少不规范代码
     *
     * @param lockName       锁名称
     * @param unit           时间单位
     * @param waitDuration   等待时长，等到最大时间，【强行】获取锁
     * @param holdDuration   锁持有时长
     * @param lockedRunnable 锁内代码
     * @return 是否实际拿到锁
     */
    boolean executeWithLock(String lockName, TimeUnit unit, int waitDuration, int holdDuration,
                            Runnable lockedRunnable);

    /**
     * 编程式获取、释放分布式锁
     * 规范化分布式锁的使用方式，减少不规范代码
     *
     * @param lockName         锁名称
     * @param unit             时间单位
     * @param waitDuration     等待时长，等到最大时间，【强行】获取锁
     * @param holdDuration     锁持有时长
     * @param lockedRunnable   锁内代码
     * @param exceptionHandler （可选）锁内代码异常处理
     * @return 是否实际拿到锁
     */
    boolean executeWithLock(String lockName, TimeUnit unit, int waitDuration, int holdDuration,
                            Runnable lockedRunnable, Consumer<Exception> exceptionHandler);
}
