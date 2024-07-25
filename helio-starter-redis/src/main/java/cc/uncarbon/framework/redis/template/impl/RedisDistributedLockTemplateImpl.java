package cc.uncarbon.framework.redis.template.impl;

import cc.uncarbon.framework.redis.lock.RedisDistributedLock;
import cc.uncarbon.framework.redis.template.RedisDistributedLockTemplate;
import cn.hutool.core.lang.Assert;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis分布式可重入锁模板类实现
 *
 * @author Uncarbon
 */
@ConditionalOnMissingBean(value = RedisDistributedLockTemplate.class)
@ConditionalOnBean(value = RedisDistributedLock.class)
@RequiredArgsConstructor
public class RedisDistributedLockTemplateImpl implements RedisDistributedLockTemplate {

    private final RedisDistributedLock redisDistributedLock;


    @Override
    public void executeWithLock(@Nonnull String lockName, @Nonnull TimeUnit unit, int holdDuration, @Nonnull Runnable lockedRunnable) {
        executeWithLock(lockName, unit, holdDuration, lockedRunnable, null);
    }

    @Override
    public void executeWithLock(@Nonnull String lockName, @Nonnull TimeUnit unit, int holdDuration, @Nonnull Runnable lockedRunnable, @Nullable Consumer<Exception> exceptionHandler) {
        Assert.isTrue(holdDuration > 0, "holdDuration必须大于0");
        RLock lock = redisDistributedLock.lock(lockName, unit, holdDuration);
        Assert.notNull(lock, "获取{}的锁对象失败", lockName);

        runAndAutoUnlock(lockName, lockedRunnable, exceptionHandler);
    }

    @Override
    public boolean executeWithLock(@Nonnull final String lockName, @Nonnull final TimeUnit unit, int waitDuration, int holdDuration,
                                   @Nonnull final Runnable lockedRunnable) {
        return executeWithLock(lockName, unit, waitDuration, holdDuration, lockedRunnable, null);
    }

    @Override
    public boolean executeWithLock(final @Nonnull String lockName, final @Nonnull TimeUnit unit, int waitDuration, int holdDuration,
                                   final @Nonnull Runnable lockedRunnable, final @Nullable Consumer<Exception> exceptionHandler) {
        Assert.isTrue(waitDuration > 0, "waitDuration必须大于0");
        Assert.isTrue(holdDuration > 0, "holdDuration必须大于0");

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
