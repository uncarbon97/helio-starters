package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.exception.FieldCryptoException;
import cc.uncarbon.framework.helium.db.fieldcrypto.hkdf.HKDF;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字段加密器：AES-256-GCM（认证加密）+ 根密钥 HKDF 按 keyId 派生 data-key
 *
 * <p>密文自带 keyId 前缀，支持多代密钥并存与轮换：</p>
 * <pre>{@code
 * {keyId}${base64(nonce(12B) || ciphertext || GCM-tag(16B))}     例: k1$Xk9s...==
 * }</pre>
 *
 * <p>加密使用活跃 keyId，解密按密文前缀路由 data-key，
 * 一经正式使用，不得变更，且不同字段密文互不可挪用
 *
 * <p>注：线程安全
 *
 * @author Uncarbon
 */
public final class FieldCipher {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 数据加密根密钥副本（仅用于 canary 指纹派生）
     */
    private final byte[] dataRootKey;
    private final Map<String, SecretKey> dataKeys;
    private final String activeKeyId;

    /**
     * @param dataRootKey 数据加密根密钥，须为 32 字节
     * @param keyIds      可用 keyId 集合，每个派生独立 data-key
     * @param activeKeyId 加密使用的活跃 keyId，必须在 keyIds 中
     */
    public FieldCipher(byte[] dataRootKey, Collection<String> keyIds, String activeKeyId) {
        if (dataRootKey == null || dataRootKey.length != FieldCryptoConstant.ROOT_KEY_BYTES) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_KEY,
                    "数据加密根密钥长度不合法，须为 " + FieldCryptoConstant.ROOT_KEY_BYTES + " 字节");
        }
        if (keyIds == null || keyIds.isEmpty()) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_KEY, "keyId 集合不能为空");
        }
        if (activeKeyId == null || activeKeyId.isBlank() || !keyIds.contains(activeKeyId)) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_KEY,
                    "activeKeyId 不能为空，且必须存在于 keyId 集合中");
        }

        this.dataRootKey = dataRootKey.clone();
        Map<String, SecretKey> tmp = new LinkedHashMap<>(keyIds.size());
        keyIds.forEach(keyId -> {
            requireValidKeyId(keyId);
            byte[] dataKeyBytes = HKDF.derive(
                    this.dataRootKey, HKDF.utf8(keyId),
                    FieldCryptoConstant.DATA_KEY_INFO_PREFIX + keyId);
            tmp.put(keyId, new SecretKeySpec(dataKeyBytes, FieldCryptoConstant.AES_ALGORITHM));
        });

        this.dataKeys = tmp;
        this.activeKeyId = activeKeyId;
    }

    /**
     * 加密（使用活跃 keyId 对应 data-key）
     *
     * @param plain 明文
     * @param aad   字段唯一编码
     * @return null 或空白入参原样返回 null；其余返回带 keyId 前缀的密文
     */
    public String encrypt(String plain, String aad) {
        requireAad(aad);
        if (plain == null || plain.isBlank()) {
            return null;
        }

        try {
            byte[] nonce = new byte[FieldCryptoConstant.GCM_NONCE_BYTES];
            RANDOM.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(FieldCryptoConstant.AES_GCM_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, dataKeys.get(activeKeyId),
                    new GCMParameterSpec(FieldCryptoConstant.GCM_TAG_BITS, nonce), null);
            cipher.updateAAD(aad.getBytes(StandardCharsets.US_ASCII));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] blob = new byte[nonce.length + encrypted.length];
            System.arraycopy(nonce, 0, blob, 0, nonce.length);
            System.arraycopy(encrypted, 0, blob, nonce.length, encrypted.length);

            return activeKeyId + FieldCryptoConstant.CIPHERTEXT_SEPARATOR
                    + Base64.getEncoder().encodeToString(blob);
        } catch (Exception e) {
            FieldCryptoMetrics.incrementEncryptFailures();
            // 不透传底层异常消息，避免泄露密钥/明文片段
            throw new FieldCryptoException(FieldCryptoException.Reason.ENCRYPT_FAILED, "字段加密失败", e);
        }
    }

    private static void requireValidKeyId(String keyId) {
        if (keyId == null || !FieldCryptoConstant.KEY_ID_PATTERN.matcher(keyId).matches()) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_KEY_ID,
                    "keyId 字符集不合法（限 [A-Za-z0-9_-]{1,16}）");
        }
    }

    private static void requireAad(String aad) {
        if (aad == null || aad.isBlank()) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_AAD,
                    "字段加密 AAD 未绑定：必须传入列级 AAD（约定取值 表名.密文列名）");
        }
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    /**
     * 数据加密根密钥代际指纹（canary 指纹之一）
     *
     * <p>同一根密钥必得同一指纹，不含密钥材料；用于节点启动时暴露根密钥配置不一致等静默漂移</p>
     */
    public String dataRootKeyCanaryFingerprint() {
        byte[] canaryDataKey = HKDF.derive(
                dataRootKey, HKDF.utf8(FieldCryptoConstant.CANARY_KEY_ID),
                FieldCryptoConstant.DATA_KEY_INFO_PREFIX + FieldCryptoConstant.CANARY_KEY_ID);
        return HKDF.sha256Hex(canaryDataKey);
    }

    public String getActiveKeyId() {
        return activeKeyId;
    }

    /**
     * 解密（按密文前缀路由 data-key，GCM 校验防篡改，AAD 按列绑定）
     *
     * @param blob 密文
     * @param aad  与加密时相同的列级 AAD；列不符即认证失败
     * @return null 或空白入参返回 null
     */
    public String decrypt(String blob, String aad) {
        requireAad(aad);
        if (blob == null || blob.isBlank()) {
            return null;
        }

        int separatorIndex = blob.indexOf(FieldCryptoConstant.CIPHERTEXT_SEPARATOR);
        if (separatorIndex <= 0 || separatorIndex == blob.length() - 1) {
            throw new FieldCryptoException(FieldCryptoException.Reason.MALFORMED_CIPHERTEXT,
                    "密文缺少 keyId 前缀或负载为空");
        }
        String keyId = blob.substring(0, separatorIndex);
        SecretKey key = dataKeys.get(keyId);
        if (key == null) {
            // keyId 非机密信息，可安全输出，便于排查轮换配置遗漏
            throw new FieldCryptoException(FieldCryptoException.Reason.KEY_NOT_FOUND,
                    "密文前缀对应的密钥未配置: " + keyId);
        }

        try {
            byte[] payload = Base64.getDecoder().decode(blob.substring(separatorIndex + 1));
            if (payload.length <= FieldCryptoConstant.GCM_NONCE_BYTES) {
                throw new FieldCryptoException(FieldCryptoException.Reason.MALFORMED_CIPHERTEXT,
                        "密文负载长度不合法");
            }

            Cipher cipher = Cipher.getInstance(FieldCryptoConstant.AES_GCM_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key,
                    new GCMParameterSpec(FieldCryptoConstant.GCM_TAG_BITS, payload, 0,
                            FieldCryptoConstant.GCM_NONCE_BYTES), null);
            cipher.updateAAD(aad.getBytes(StandardCharsets.US_ASCII));
            byte[] decrypted = cipher.doFinal(payload, FieldCryptoConstant.GCM_NONCE_BYTES,
                    payload.length - FieldCryptoConstant.GCM_NONCE_BYTES);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (FieldCryptoException e) {
            throw e;
        } catch (Exception e) {
            FieldCryptoMetrics.incrementDecryptFailures();
            FieldCryptoMetrics.incrementGcmTagVerifyFailures();
            throw new FieldCryptoException(FieldCryptoException.Reason.DECRYPT_FAILED,
                    "字段解密失败（密钥不符、AAD 列绑定不符或数据被篡改）", e);
        }
    }
}
