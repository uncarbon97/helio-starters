package cc.uncarbon.framework.helium.db.fieldcrypto.blindindex;

import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.exception.FieldCryptoException;
import cc.uncarbon.framework.helium.db.fieldcrypto.hkdf.HKDF;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 盲索引计算器：HMAC-SHA256 密钥化哈希，索引根密钥按用途 HKDF 派生 index-key，截断 128bit
 *
 * <p>用于密文列旁的等值检索列；密钥化哈希可抵抗低熵字段（手机号、身份证号）的穷举/彩虹表攻击
 * 用途约定 {@code 表.列.用途}（如 {@code sys_user.phone_no.full}），每用途独立 index-key，跨列隔离
 * 索引值为单代密钥、不带版本前缀，轮换须停写重建
 *
 * <p>算法参数见 {@link FieldCryptoConstant}（冻结常量）
 *
 * <p>注：线程安全
 *
 * @author Uncarbon
 */
public final class BlindIndex {

    private final SecretKeySpec rootKeySpec;

    /**
     * 用途 -> 派生 index-key 缓存
     */
    private final Map<String, SecretKeySpec> indexKeyCache = new ConcurrentHashMap<>();

    /**
     * @param indexRootKey 索引根密钥，须为 32 字节
     */
    public BlindIndex(byte[] indexRootKey) {
        if (indexRootKey == null || indexRootKey.length != FieldCryptoConstant.ROOT_KEY_BYTES) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_KEY,
                    "索引根密钥长度不合法，须为 " + FieldCryptoConstant.ROOT_KEY_BYTES + " 字节");
        }
        this.rootKeySpec = new SecretKeySpec(indexRootKey.clone(), FieldCryptoConstant.MAC_ALGORITHM);
    }

    private static void requirePurpose(String purpose) {
        if (purpose == null || !FieldCryptoConstant.PURPOSE_PATTERN.matcher(purpose).matches()) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_PURPOSE,
                    "盲索引用途标识不合法: " + purpose + "（约定取值 表.列.用途，如 sys_user.phone_no.full）");
        }
    }

    /**
     * 计算盲索引值
     *
     * @param purpose         用途标识（如 sys_user.phone_no.full），与实体注解声明保持一致
     * @param normalizedValue 归一化后的明文
     * @return 入参为 null/空白时返回 null；其余返回 Base64(HMAC-SHA256 截断 128bit)，最终长度 = 24 且以 `==` 结尾
     */
    public String compute(String purpose, String normalizedValue) {
        requirePurpose(purpose);
        if (normalizedValue == null || normalizedValue.isBlank()) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance(FieldCryptoConstant.MAC_ALGORITHM);
            mac.init(deriveIndexKey(purpose));
            byte[] digest = mac.doFinal(normalizedValue.getBytes(StandardCharsets.UTF_8));
            String ret = Base64.getEncoder()
                    .encodeToString(Arrays.copyOf(digest, FieldCryptoConstant.BLIND_INDEX_TRUNCATED_BYTES));
            assert ret.length() == FieldCryptoConstant.BLIND_INDEX_VALUE_LENGTH && ret.endsWith("==");
            return ret;
        } catch (Exception e) {
            throw new FieldCryptoException(FieldCryptoException.Reason.DECRYPT_FAILED, "盲索引计算失败", e);
        }
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    /**
     * 索引根密钥代际指纹（canary 指纹之一）
     *
     * <p>同一根密钥必得同一指纹，不含密钥材料；用于节点启动时暴露多节点密钥配置不一致等静默漂移</p>
     */
    public String indexRootKeyCanaryFingerprint() {
        byte[] indexKey = HKDF.derive(rootKeyBytes(), HKDF.utf8(FieldCryptoConstant.INDEX_KEY_SALT),
                FieldCryptoConstant.INDEX_KEY_INFO_PREFIX + FieldCryptoConstant.CANARY_KEY_ID);
        return HKDF.sha256Hex(indexKey);
    }

    private SecretKeySpec deriveIndexKey(String purpose) {
        return indexKeyCache.computeIfAbsent(purpose, p -> {
            byte[] indexKey = HKDF.derive(
                    rootKeyBytes(), HKDF.utf8(FieldCryptoConstant.INDEX_KEY_SALT),
                    FieldCryptoConstant.INDEX_KEY_INFO_PREFIX + p);
            return new SecretKeySpec(indexKey, FieldCryptoConstant.MAC_ALGORITHM);
        });
    }

    private byte[] rootKeyBytes() {
        return rootKeySpec.getEncoded();
    }
}
