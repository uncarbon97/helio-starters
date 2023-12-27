package cc.uncarbon.framework.redis.support.impl;

import cc.uncarbon.framework.redis.lock.RedisDistributedLock;
import cc.uncarbon.framework.redis.support.RedisDistributedLockSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis分布式可重入锁辅助类实现
 *
 * @author Uncarbon
 */
@ConditionalOnBean(value = RedisDistributedLock.class)
@RequiredArgsConstructor
public class RedisDistributedLockSupportImpl implements RedisDistributedLockSupport {

    private final RedisDistributedLock redisDistributedLock;


    @Override
    public boolean executeWithLock(final String lockName, final TimeUnit unit, int waitDuration, int holdDuration,
                                   final Runnable lockedRunnable) {
        return executeWithLock(lockName, unit, waitDuration, holdDuration, lockedRunnable, null);
    }

    @Override
    public boolean executeWithLock(final String lockName, final TimeUnit unit, int waitDuration, int holdDuration,
                                   final Runnable lockedRunnable, final Consumer<Exception> exceptionHandler) {
        // corner case
        if (Objects.isNull(lockName) || Objects.isNull(unit) || waitDuration < 0 || holdDuration < 0
                || Objects.isNull(lockedRunnable)) {
            throw new IllegalArgumentException("Required arguments are null");
        }

        boolean lockedFlag = redisDistributedLock.tryLock(lockName, unit, waitDuration, holdDuration);
        if (!lockedFlag) {
            return false;
        }
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
        return true;
    }
}
