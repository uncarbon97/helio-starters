package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import cc.uncarbon.framework.helium.db.fieldcrypto.exception.FieldCryptoException;

/**
 * FieldCipher 静态注册上下文
 * 未注册即使用时快速失败，绝不静默回退为明文落库
 *
 * @author Uncarbon
 */
public final class FieldCipherContext {

    private static volatile FieldCipher instance;

    private FieldCipherContext() {
    }

    public static void register(FieldCipher fieldCipher) {
        instance = fieldCipher;
    }

    public static boolean isRegistered() {
        return instance != null;
    }

    /**
     * @return 已注册的 FieldCipher；未注册时抛出语义化异常
     */
    public static FieldCipher getRequired() {
        FieldCipher ret = instance;
        if (ret == null) {
            throw new FieldCryptoException(FieldCryptoException.Reason.NOT_CONFIGURED,
                    "FieldCipher 未注册：请检查是否配置了 helium.db.field-crypto.* 或提供了 FieldKeyProvider Bean");
        }
        return ret;
    }

    /**
     * 仅供测试使用
     */
    public static void clear() {
        instance = null;
    }
}
