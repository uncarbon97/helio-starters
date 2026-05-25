package cc.uncarbon.framework.helium.redis.lock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式可重入锁
 *
 * @author dcy
 * @author Uncarbon
 */
public interface RedisDistributedLock {

    /**
     * 获取分布式锁，等待时长无限
     *
     * @param lockName     锁名称
     * @param holdDuration 锁持有时长，单位：秒；【注意】持有时长一定要大于业务的执行时间，锁不会自动续期
     * @return 锁对象
     */
    RLock lock(String lockName, int holdDuration);

    /**
     * 获取分布式锁，等待时长无限
     *
     * @param lockName 锁名称
     * @param unit 时间单位
     * @param holdDuration 锁持有时长；【注意】持有时长一定要大于业务的执行时间，锁不会自动续期
     * @return 锁对象
     */
    RLock lock(String lockName, TimeUnit unit, int holdDuration);

    /**
     * 尝试获取锁，在等待时长内获取到锁则返回true，否则返回false
     *
     * @param lockName     锁名称
     * @param waitDuration 最大等待时长
     * @param holdDuration 锁持有时长，单位：秒；【注意】持有时长一定要大于业务的执行时间，锁不会自动续期
     * @return 锁对象
     */
    boolean tryLock(String lockName, int waitDuration, int holdDuration);

    /**
     * 尝试获取锁，在等待时长内获取到锁则返回true，否则返回false
     *
     * @param lockName     锁名称
     * @param unit         时间单位
     * @param waitDuration 最大等待时长
     * @param holdDuration 锁持有时长；【注意】持有时长一定要大于业务的执行时间，锁不会自动续期
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

    /**
     * 修改分布式锁对应 redis-key 的前缀
     * 默认值为【distributed-lock:】
     */
    void setLockKeyPrefix(String newPrefix);

}
