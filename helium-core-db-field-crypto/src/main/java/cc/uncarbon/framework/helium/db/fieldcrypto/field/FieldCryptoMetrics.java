package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 字段加解密运行指标（进程内原子计数，零第三方依赖）
 *
 * <p>应用侧可周期性采集快照接入监控；GCM tag 校验失败率异常升高通常意味着密文被篡改或根密钥代际漂移
 *
 * @author Uncarbon
 */
public final class FieldCryptoMetrics {

    private static final AtomicLong ENCRYPT_FAILURES = new AtomicLong();
    private static final AtomicLong DECRYPT_FAILURES = new AtomicLong();
    private static final AtomicLong GCM_TAG_VERIFY_FAILURES = new AtomicLong();
    private static final AtomicLong DERIVED_COLUMN_REJECTS = new AtomicLong();

    private FieldCryptoMetrics() {
    }

    public static void incrementEncryptFailures() {
        ENCRYPT_FAILURES.incrementAndGet();
    }

    public static void incrementDecryptFailures() {
        DECRYPT_FAILURES.incrementAndGet();
    }

    /**
     * GCM 认证标签校验失败（解密失败中专门归因于 tag/认证失败的部分）
     */
    public static void incrementGcmTagVerifyFailures() {
        GCM_TAG_VERIFY_FAILURES.incrementAndGet();
    }

    /**
     * 派生列一致性校验拦截器拒写次数
     */
    public static void incrementDerivedColumnRejects() {
        DERIVED_COLUMN_REJECTS.incrementAndGet();
    }

    public static long snapshotEncryptFailures() {
        return ENCRYPT_FAILURES.get();
    }

    public static long snapshotDecryptFailures() {
        return DECRYPT_FAILURES.get();
    }

    public static long snapshotGcmTagVerifyFailures() {
        return GCM_TAG_VERIFY_FAILURES.get();
    }

    public static long snapshotDerivedColumnRejects() {
        return DERIVED_COLUMN_REJECTS.get();
    }
}
