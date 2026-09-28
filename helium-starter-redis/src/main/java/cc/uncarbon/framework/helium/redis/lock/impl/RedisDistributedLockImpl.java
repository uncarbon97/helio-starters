package cc.uncarbon.framework.helium.redis.lock.impl;

import cc.uncarbon.framework.helium.redis.lock.RedisDistributedLock;
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
        boolean locked = false;
        try {
            locked = lock.tryLock(waitDuration, holdDuration, unit);
        } catch (InterruptedException ex) {
            // 恢复中断标志，保留线程池优雅停机/取消语义
            Thread.currentThread().interrupt();
        }
        return locked;
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
            try {
                this.unlock(lock);
            } catch (IllegalMonitorStateException _) {
                /*
                检查通过后、释放前锁恰好过期（检查与释放是多次 Redis 往返）：
                锁已不属于本线程，释放无意义；吞掉，避免从 finally 抛出覆盖正在传播的业务异常
                 */
            }
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
