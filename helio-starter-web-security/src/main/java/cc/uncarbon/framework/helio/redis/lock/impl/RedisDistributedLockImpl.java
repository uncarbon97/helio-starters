package cc.uncarbon.framework.helio.redis.lock.impl;

import cc.uncarbon.framework.helio.redis.lock.RedisDistributedLock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Redis 分布式可重入锁，基于 Redisson 实现
 *
 * @author dcy
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class RedisDistributedLockImpl implements RedisDistributedLock {

    /**
     * 锁名称前缀
     */
    private static final AtomicReference<String> LOCK_KEY_PREFIX = new AtomicReference<>("distributed-lock:");

    private final RedissonClient redissonClient;


    @Override
    public RLock lock(String lockName, int holdDuration) {
        return this.lock(lockName, TimeUnit.SECONDS, holdDuration);
    }

    @Override
    public RLock lock(String lockName, TimeUnit unit, int holdDuration) {
        RLock lock = this.getRLockByName(lockName);
        lock.lock(holdDuration, unit);

        return lock;
    }

    @Override
    public boolean tryLock(String lockName, int waitDuration, int holdDuration) {
        return this.tryLock(lockName, TimeUnit.SECONDS, waitDuration, holdDuration);
    }

    @Override
    public boolean tryLock(String lockName, TimeUnit unit, int waitDuration, int holdDuration) {
        RLock lock = this.getRLockByName(lockName);
        try {
            return lock.tryLock(waitDuration, holdDuration, unit);
        } catch (InterruptedException ex) {
            return false;
        }
    }

    @Override
    public void unlock(String lockName) {
        RLock lock = this.getRLockByName(lockName);
        this.unlock(lock);
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }

    @Override
    public void unlockSafely(String lockName) {
        RLock lock = this.getRLockByName(lockName);
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

    @Override
    public void setLockKeyPrefix(String newPrefix) {
        LOCK_KEY_PREFIX.set(newPrefix);
    }

    private RLock getRLockByName(String lockName) {
        return redissonClient.getLock(LOCK_KEY_PREFIX.get() + lockName);
    }
}
