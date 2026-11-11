package cc.uncarbon.framework.helium.redis.lock.impl;

import cc.uncarbon.framework.helium.redis.lock.RedisDistributedLock;
import cc.uncarbon.framework.helium.redis.lock.RedisDistributedLockTemplate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
            throw new IllegalArgumentException("holdDuration 必须大于 0");
        }
        // 要么阻塞直到拿到锁，要么抛异常，不会返回 null
        redisDistributedLock.lock(lockName, unit, holdDuration);
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
            throw new IllegalArgumentException("waitDuration 必须大于 0");
        }
        if (!(holdDuration > 0)) {
            throw new IllegalArgumentException("holdDuration 必须大于 0");
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
        } catch (Throwable t) {
            // Error（OOM/StackOverflow 等）同样要释放锁，避免锁在 TTL 到期前一直占用
            throw t;
        } finally {
            redisDistributedLock.unlockSafely(lockName);
        }
    }
}
