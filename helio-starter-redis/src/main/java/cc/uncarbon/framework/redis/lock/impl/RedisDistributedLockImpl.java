package cc.uncarbon.framework.redis.lock.impl;

import cc.uncarbon.framework.redis.lock.RedisDistributedLock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式可重入锁，基于Redisson实现
 *
 * @author dcy
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class RedisDistributedLockImpl implements RedisDistributedLock {

    /**
     * 锁名称前缀
     */
    private static final String LOCK_KEY_PREFIX = "distributedLock:";

    private final RedissonClient redissonClient;


    @Override
    public RLock lock(String lockName, int holdDuration) {
        return this.lock(lockName, TimeUnit.SECONDS, holdDuration);
    }

    @Override
    public RLock lock(String lockName, TimeUnit unit, int holdDuration) {
        RLock lock = this.getRedissonLockByName(lockName);
        lock.lock(holdDuration, unit);

        return lock;
    }

    @Override
    public boolean tryLock(String lockName, int waitDuration, int holdDuration) {
        return this.tryLock(lockName, TimeUnit.SECONDS, waitDuration, holdDuration);
    }

    @Override
    public boolean tryLock(String lockName, TimeUnit unit, int waitDuration, int holdDuration) {
        RLock lock = this.getRedissonLockByName(lockName);
        try {
            return lock.tryLock(waitDuration, holdDuration, unit);
        } catch (InterruptedException ex) {
            return false;
        }
    }

    @Override
    public void unlock(String lockName) {
        RLock lock = this.getRedissonLockByName(lockName);
        this.unlock(lock);
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }

    @Override
    public void unlockSafely(String lockName) {
        RLock lock = this.getRedissonLockByName(lockName);
        this.unlockSafely(lock);
    }

    @Override
    public void unlockSafely(RLock lock) {
        if (lock == null) {
            return;
        }

        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            this.unlock(lock);
        }
    }

    private RLock getRedissonLockByName(String lockName) {
        return redissonClient.getLock(LOCK_KEY_PREFIX + lockName);
    }
}
