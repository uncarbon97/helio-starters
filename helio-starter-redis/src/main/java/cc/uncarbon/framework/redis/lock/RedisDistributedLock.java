package cc.uncarbon.framework.redis.lock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式可重入锁
 *
 * @author dcy
 * @author Uncarbon
 */
public interface RedisDistributedLock {

    /**
     * 一般加锁 - 主动为锁设置持有时长
     * 注意：持有时长一定要大于业务的执行时间，锁不会自动续期
     *
     * @param lockName     锁名称
     * @param holdDuration 锁持有时长，单位：秒
     * @return 锁对象
     */
    RLock lock(String lockName, int holdDuration);


    /**
     * 一般加锁 - 主动为锁设置持有时长及单位
     * 注意：持有时长一定要大于业务的执行时间，锁不会自动续期
     *
     * @param lockName 锁名称
     * @param unit 时间单位
     * @param holdDuration 锁持有时长
     * @return 锁对象
     */
    RLock lock(String lockName, TimeUnit unit, int holdDuration);

    /**
     * 尝试获取锁，在等待时长内获取到锁则返回true，否则返回false；如果获取到锁，在锁持有时长结束后释放锁
     *
     * @param lockName     锁名称
     * @param waitDuration 等待时长，等到最大时间，【强行】获取锁
     * @param holdDuration 锁持有时长，单位：秒
     * @return 锁对象
     */
    boolean tryLock(String lockName, int waitDuration, int holdDuration);

    /**
     * 尝试获取锁，在等待时长内获取到锁则返回true，否则返回false；如果获取到锁，在锁持有时长结束后释放锁
     *
     * @param lockName     锁名称
     * @param unit         时间单位
     * @param waitDuration 等待时长，等到最大时间，【强行】获取锁
     * @param holdDuration 锁持有时长
     * @return 锁对象
     */
    boolean tryLock(String lockName, TimeUnit unit, int waitDuration, int holdDuration);

    /**
     * 主动释放锁
     * 注意：若锁并非当前线程持有，会抛出IllegalMonitorStateException异常
     * 建议使用unlockSafely
     *
     * @param lockName 锁名称
     */
    void unlock(String lockName);

    /**
     * 主动释放锁
     * 注意：若锁并非当前线程持有，会抛出IllegalMonitorStateException异常
     * 建议使用unlockSafely
     *
     * @param lock 锁对象
     */
    void unlock(RLock lock);

    /**
     * 安全地主动释放锁
     *
     * @param lockName 锁名称
     */
    void unlockSafely(String lockName);

    /**
     * 安全地主动释放锁
     *
     * @param lock 锁对象
     */
    void unlockSafely(RLock lock);
}
