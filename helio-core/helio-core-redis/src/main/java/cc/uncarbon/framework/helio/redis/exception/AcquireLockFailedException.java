package cc.uncarbon.framework.helio.redis.exception;

/**
 * 业务异常 - 获取锁对象失败
 *
 * @author Uncarbon
 */
public class AcquireLockFailedException extends RuntimeException {

    public AcquireLockFailedException() {
        super("Failed to acquire lock");
    }
}
