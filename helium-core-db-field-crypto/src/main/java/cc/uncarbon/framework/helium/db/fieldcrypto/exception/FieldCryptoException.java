package cc.uncarbon.framework.helium.db.fieldcrypto.exception;


/**
 * 字段加解密异常
 * 只携带语义化原因码与安全描述，严禁携带密钥材料、明文或密文字节，避免异常被打印到日志时造成泄露
 *
 * @author Uncarbon
 */
public class FieldCryptoException extends RuntimeException {

    private final Reason reason;

    public FieldCryptoException(Reason reason, String safeMessage) {
        super(safeMessage);
        this.reason = reason;
    }

    public FieldCryptoException(Reason reason, String safeMessage, Throwable cause) {
        // 仅保留 cause 的类型，不透传其消息（可能含密文/密钥片段）
        super(safeMessage + " (" + cause.getClass().getSimpleName() + ")", cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        /**
         * 未注册 FieldCipher（未配置 helium.db.field-crypto.*）
         */
        NOT_CONFIGURED,
        /**
         * 密钥长度/格式不合法
         */
        INVALID_KEY,
        /**
         * keyId 字符集或长度不合法（限 [A-Za-z0-9_-]{1,16}，禁止含 '$'）
         */
        INVALID_KEY_ID,
        /**
         * AAD 未绑定（空/未声明）
         */
        INVALID_AAD,
        /**
         * 盲索引用途标识不合法
         */
        INVALID_PURPOSE,
        /**
         * 密文携带的 keyId 未配置
         */
        KEY_NOT_FOUND,
        /**
         * 密文格式不合法
         */
        MALFORMED_CIPHERTEXT,
        /**
         * 加密失败
         */
        ENCRYPT_FAILED,
        /**
         * 解密失败（密钥不符 / 数据被篡改 / GCM 校验失败）
         */
        DECRYPT_FAILED,
        /**
         * 派生列与密文列不一致（拒写）
         */
        DERIVED_COLUMN_MISMATCH,
    }
}
