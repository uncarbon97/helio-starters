package cc.uncarbon.framework.helium.redis.lock.impl;

import cc.uncarbon.framework.helium.redis.lock.RedisDistributedLock;
import cc.uncarbon.framework.helium.redis.lock.RedisDistributedLockTemplate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
    public boolean executeWithLock(@NonNull String lockName, @NonNull TimeUnit unit, int holdDuration,
                                   @NonNull Runnable lockedRunnable) {
        return executeWithLock(lockName, unit, holdDuration, lockedRunnable, null);
    }

    @Override
    public boolean executeWithLock(@NonNull String lockName, @NonNull TimeUnit unit, int holdDuration,
                                   @NonNull Runnable lockedRunnable,
                                   @Nullable Consumer<Exception> exceptionHandler) {
        if (!(holdDuration > 0)) {
            throw new IllegalArgumentException("holdDuration must greater than 0");
        }
        RLock lock = redisDistributedLock.lock(lockName, unit, holdDuration);
        if (lock == null) {
            return false;
        }
        runAndAutoUnlock(lockName, lockedRunnable, exceptionHandler);
        return true;
    }

    @Override
    public boolean executeWithLock(@NonNull String lockName, @NonNull TimeUnit unit, int waitDuration,
                                   int holdDuration, @NonNull Runnable lockedRunnable) {
        return executeWithLock(lockName, unit, waitDuration, holdDuration, lockedRunnable, null);
    }

    @Override
    public boolean executeWithLock(@NonNull String lockName, @NonNull TimeUnit unit, int waitDuration,
                                   int holdDuration, @NonNull Runnable lockedRunnable,
                                   @Nullable Consumer<Exception> exceptionHandler) {
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
