package cc.uncarbon.framework.helium.redis.lock.impl;

import cc.uncarbon.framework.helium.redis.lock.RedisDistributedLock;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式可重入锁，基于 Redisson 实现
 *
 * @author dcy
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class RedisDistributedLockImpl implements RedisDistributedLock {

    /**
     * 锁名称前缀
     */
    private volatile String lockKeyPrefix = "distributed-lock:";

    private final RedissonClient redissonClient;


    @Override
    public RLock lock(String lockName, int holdDuration) {
        return this.lock(lockName, TimeUnit.SECONDS, holdDuration);
    }

    @Override
    public boolean tryLock(String lockName, int waitDuration, int holdDuration) {
        return this.tryLock(lockName, TimeUnit.SECONDS, waitDuration, holdDuration);
    }

    protected static void requirePositiveHoldDuration(int holdDuration) {
        if (holdDuration <= 0) {
            throw new IllegalArgumentException("holdDuration 必须大于 0（不支持 -1 看门狗续期），当前: " + holdDuration);
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
    public RLock lock(String lockName, TimeUnit unit, int holdDuration) {
        requirePositiveHoldDuration(holdDuration);
        RLock lock = this.getRLockByName(lockName);
        lock.lock(holdDuration, unit);

        return lock;
    }

    @Override
    public boolean tryLock(String lockName, TimeUnit unit, int waitDuration, int holdDuration) {
        requirePositiveHoldDuration(holdDuration);
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
    public void unlockSafely(RLock lock) {
        if (lock == null) {
            return;
        }

        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            try {
                this.unlock(lock);
            } catch (RuntimeException e) {
                /*
                兜住两类情况：
                1. 检查通过后、释放前锁恰好过期（IllegalMonitorStateException）—— 锁已不属于本线程
                2. Redis 恰好断连等系统异常 —— 从 finally 冒出会覆盖正在传播的业务异常或正常返回
                安全释放不回滚业务，单行 warn 留痕
                 */
                log.warn("[RedisDistributedLock] 安全解锁失败，已吞掉: {}", e.toString());
            }
        }
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    @Override
    public void setLockKeyPrefix(@NonNull String newPrefix) {
        if (newPrefix.isBlank()) {
            throw new IllegalArgumentException("新前缀必须为有效字符");
        }
        this.lockKeyPrefix = newPrefix;
    }

    protected RLock getRLockByName(String lockName) {
        if (CharSequenceUtil.isBlank(lockName)) {
            // 拒绝空锁名：拼接 null 会得到 "prefix:null"，所有传 null 的业务互抢同一把锁
            throw new IllegalArgumentException("锁名称不能为空");
        }
        return redissonClient.getLock(lockKeyPrefix + lockName);
    }
}
