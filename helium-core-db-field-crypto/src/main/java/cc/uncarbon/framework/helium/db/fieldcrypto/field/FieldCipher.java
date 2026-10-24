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
 * <p>加密使用活跃 keyId，解密按密文前缀路由 data-key（当前代未命中时回落上一代根密钥，
 * 仅供数据根泄露应急轮换期兜底解密），
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

    /**
     * 上一代根密钥派生的 data-key（keyId -> key），仅供解密路由；无应急轮换时为空 Map
     */
    private final Map<String, SecretKey> previousDataKeys;
    private final String activeKeyId;

    /**
     * @param dataRootKey 数据加密根密钥，须为 32 字节
     * @param keyIds      可用 keyId 集合，每个派生独立 data-key
     * @param activeKeyId 加密使用的活跃 keyId，必须在 keyIds 中
     */
    public FieldCipher(byte[] dataRootKey, Collection<String> keyIds, String activeKeyId) {
        this(dataRootKey, keyIds, activeKeyId, null, null);
    }

    /**
     * 数据根密钥泄露应急轮换构造：当前根密钥 + 上一代根密钥并存
     *
     * @param previousDataRootKey 上一代数据根密钥，与 previousKeyIds 成对出现（同时为空 = 无应急轮换）
     * @param previousKeyIds      上一代 keyId 集合，按上一代根密钥派生，仅供解密路由；不得与 keyIds 交集
     */
    public FieldCipher(byte[] dataRootKey, Collection<String> keyIds, String activeKeyId,
                       byte[] previousDataRootKey, Collection<String> previousKeyIds) {
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
        this.previousDataKeys = derivePreviousDataKeys(previousDataRootKey, previousKeyIds);
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
     * @return 本节点能否解密该 keyId 前缀的密文（当前代或上一代集合任一命中）
     */
    public boolean canDecrypt(String keyId) {
        return dataKeys.containsKey(keyId) || previousDataKeys.containsKey(keyId);
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
        // 当前代优先，未命中回落上一代（数据根应急轮换期旧前缀密文兜底解密）
        SecretKey key = dataKeys.get(keyId);
        if (key == null) {
            key = previousDataKeys.get(keyId);
        }
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

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    private static void requireValidKeyId(String keyId) {
        if (keyId == null || !FieldCryptoConstant.KEY_ID_PATTERN.matcher(keyId).matches()) {
            throw new FieldCryptoException(FieldCryptoException.Reason.ILLEGAL_KEY_ID,
                    "keyId 字符集不合法（限 [A-Za-z0-9-]{1,16}）");
        }
    }

    private static void requireAad(String aad) {
        if (aad == null || aad.isBlank()) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_AAD,
                    "字段加密 AAD 未绑定：必须传入列级 AAD（约定取值 表名.密文列名）");
        }
    }

    private Map<String, SecretKey> derivePreviousDataKeys(byte[] previousDataRootKey, Collection<String> previousKeyIds) {
        boolean hasPrevRoot = previousDataRootKey != null && previousDataRootKey.length > 0;
        boolean hasPrevIds = previousKeyIds != null && !previousKeyIds.isEmpty();
        if (!hasPrevRoot && !hasPrevIds) {
            return Map.of();
        }
        if (hasPrevRoot != hasPrevIds) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_KEY,
                    FieldCryptoConstant.PREVIOUS_DATA_ROOT_KEY_KEBAB + " 与 previous-data-key-ids 必须成对配置");
        }
        if (previousDataRootKey.length != FieldCryptoConstant.ROOT_KEY_BYTES) {
            throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_KEY,
                    "上一代数据根密钥长度不合法，须为 " + FieldCryptoConstant.ROOT_KEY_BYTES + " 字节");
        }

        Map<String, SecretKey> prevTmp = new LinkedHashMap<>(previousKeyIds.size());
        for (String keyId : previousKeyIds) {
            requireValidKeyId(keyId);
            if (dataKeys.containsKey(keyId)) {
                // 同一 keyId 挂两代根密钥 = 派生歧义，轮换期配置事故
                throw new FieldCryptoException(FieldCryptoException.Reason.INVALID_KEY,
                        "keyId 同时存在于当前代与上一代集合: " + keyId);
            }
            byte[] dataKeyBytes = HKDF.derive(
                    previousDataRootKey, HKDF.utf8(keyId),
                    FieldCryptoConstant.DATA_KEY_INFO_PREFIX + keyId);
            prevTmp.put(keyId, new SecretKeySpec(dataKeyBytes, FieldCryptoConstant.AES_ALGORITHM));
        }
        return prevTmp;
    }
}
