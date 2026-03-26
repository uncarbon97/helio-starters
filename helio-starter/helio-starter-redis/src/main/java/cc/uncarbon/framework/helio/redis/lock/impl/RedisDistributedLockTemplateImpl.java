package cc.uncarbon.framework.helio.redis.lock.impl;

import cc.uncarbon.framework.helio.redis.exception.AcquireLockFailedException;
import cc.uncarbon.framework.helio.redis.lock.RedisDistributedLock;
import cc.uncarbon.framework.helio.redis.lock.RedisDistributedLockTemplate;
import cn.hutool.core.lang.Assert;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis 分布式可重入锁，编程式使用方式
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class RedisDistributedLockTemplateImpl implements RedisDistributedLockTemplate {

    private final RedisDistributedLock redisDistributedLock;


    @Override
    public void executeWithLock(@Nonnull String lockName, @Nonnull TimeUnit unit, int holdDuration,
                                @Nonnull Runnable lockedRunnable) throws AcquireLockFailedException {
        executeWithLock(lockName, unit, holdDuration, lockedRunnable, null);
    }

    @Override
    public void executeWithLock(@Nonnull String lockName, @Nonnull TimeUnit unit, int holdDuration,
                                @Nonnull Runnable lockedRunnable,
                                @Nullable Consumer<Exception> exceptionHandler) throws AcquireLockFailedException {
        if (!(holdDuration > 0)) {
            throw new IllegalArgumentException("holdDuration must greater than 0");
        }
        RLock lock = redisDistributedLock.lock(lockName, unit, holdDuration);
        if (lock == null) {
            throw new AcquireLockFailedException();
        }

        runAndAutoUnlock(lockName, lockedRunnable, exceptionHandler);
    }

    @Override
    public boolean executeWithLock(@Nonnull final String lockName, @Nonnull final TimeUnit unit, int waitDuration, int holdDuration,
                                   @Nonnull final Runnable lockedRunnable) {
        return executeWithLock(lockName, unit, waitDuration, holdDuration, lockedRunnable, null);
    }

    @Override
    public boolean executeWithLock(final @Nonnull String lockName, final @Nonnull TimeUnit unit, int waitDuration, int holdDuration,
                                   final @Nonnull Runnable lockedRunnable,
                                   final @Nullable Consumer<Exception> exceptionHandler){
        if (!(waitDuration > 0)) {
            throw new IllegalArgumentException("waitDuration must greater than 0");
        }
        if (!(holdDuration > 0)) {
            throw new IllegalArgumentException("holdDuration must greater than 0");
        }

        boolean lockedFlag = redisDistributedLock.tryLock(lockName, unit, waitDuration, holdDuration);
        if (!lockedFlag) {
            return false;
        }
        runAndAutoUnlock(lockName, lockedRunnable, exceptionHandler);
        return true;
    }

    protected void runAndAutoUnlock(String lockName, Runnable lockedRunnable, Consumer<Exception> exceptionHandler) {
        try {
            lockedRunnable.run();
        } catch (Exception e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            } else {
                throw e;
            }
        } finally {
            redisDistributedLock.unlockSafely(lockName);
        }
    }
}
