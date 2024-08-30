package cc.uncarbon.framework.redis.template;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis分布式可重入锁模板类
 * 编程式获取分布式锁，模板化分布式锁的使用方式
 *
 * @author Uncarbon
 */
public interface RedisDistributedLockTemplate {

    /**
     * 获取分布式锁并执行锁内代码，等待时长无限
     *
     * @param lockName       锁名称
     * @param unit           时间单位
     * @param holdDuration   锁持有时长；【注意】持有时长一定要大于业务的执行时间，锁不会自动续期
     * @param lockedRunnable 锁内代码，建议单独开个子方法，通过lambda表达式引用
     */
    void executeWithLock(@Nonnull String lockName, @Nonnull TimeUnit unit, int holdDuration,
                         @Nonnull Runnable lockedRunnable);

    /**
     * 获取分布式锁并执行锁内代码，等待时长无限
     *
     * @param lockName         锁名称
     * @param unit             时间单位
     * @param holdDuration     锁持有时长；【注意】持有时长一定要大于业务的执行时间，锁不会自动续期
     * @param lockedRunnable   锁内代码，建议单独开个子方法，通过lambda表达式引用
     * @param exceptionHandler （可选）锁内代码异常处理过程
     */
    void executeWithLock(@Nonnull String lockName, @Nonnull TimeUnit unit, int holdDuration,
                         @Nonnull Runnable lockedRunnable, @Nullable Consumer<Exception> exceptionHandler);

    /**
     * 获取分布式锁并执行锁内代码
     * 【注意】务必考虑到锁内代码有抛出异常的可能
     *
     * @param lockName       锁名称
     * @param unit           时间单位
     * @param waitDuration   最大等待时长
     * @param holdDuration   锁持有时长；【注意】持有时长一定要大于业务的执行时间，锁不会自动续期
     * @param lockedRunnable 锁内代码，建议单独开个子方法，通过lambda表达式引用
     * @return 是否实际拿到锁
     */
    boolean executeWithLock(@Nonnull String lockName, @Nonnull TimeUnit unit, int waitDuration, int holdDuration,
                            @Nonnull Runnable lockedRunnable);

    /**
     * 获取分布式锁并执行锁内代码
     * 可手动指定锁内代码异常处理过程
     *
     * @param lockName         锁名称
     * @param unit             时间单位
     * @param waitDuration     最大等待时长
     * @param holdDuration     锁持有时长；【注意】持有时长一定要大于业务的执行时间，锁不会自动续期
     * @param lockedRunnable   锁内代码，建议单独开个子方法，通过lambda表达式引用
     * @param exceptionHandler （可选）锁内代码异常处理过程
     * @return 是否实际拿到锁
     */
    boolean executeWithLock(@Nonnull String lockName, @Nonnull TimeUnit unit, int waitDuration, int holdDuration,
                            @Nonnull Runnable lockedRunnable, @Nullable Consumer<Exception> exceptionHandler);
}
