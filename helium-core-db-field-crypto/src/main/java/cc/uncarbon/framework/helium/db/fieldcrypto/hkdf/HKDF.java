package cc.uncarbon.framework.helium.db.fieldcrypto.hkdf;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * HKDF（HMAC-based Key Derivation Function, RFC 5869）
 * 从一个主密钥 + 一段用途标签，确定性推出一把新密钥
 * 同一主密钥、不同标签 → 不同子密钥，且子密钥之间互不可推
 * <P>好处：
 * <UL>
 * <LI>一钥一用途：加密数据的 key 和算索引的 key 永远不是同一把；手机号全串索引和尾号索引也不是同一把。一把泄露不殃及其他（纵深防御，CipherSweet per-field key 惯例）</LI>
 * <LI>免分发：子密钥现场推算，无须持久化存储</LI>
 * <LI>免密钥库：密钥全在内存，拖库拖不走密钥</LI>
 * </UL>
 * <P>弊端：
 * 根密钥如果丢失，历史派生出的所有子密钥均不安全
 *
 * @author Uncarbon
 */
public final class HKDF {

    private static final String MAC_ALGORITHM = "HmacSHA256";
    private static final int HASH_LEN = 32;
    private static final int MAX_OUTPUT_BYTES = 255 * HASH_LEN;
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private HKDF() {
    }

    /**
     * @param rootKey 根密钥
     * @param salt    盐（null 等价于 HashLen 个零字节）
     * @param purpose 用途标签（可为 null）
     */
    public static byte[] derive(byte @NonNull [] rootKey, byte @Nullable [] salt, @Nullable String purpose) {
        if (rootKey.length == 0) {
            throw new IllegalStateException("根密钥不能为空");
        }
        byte[] finalSalt = (salt == null) ? new byte[0] : salt;
        byte[] finalPurpose = (purpose == null) ? new byte[0] : purpose.getBytes(StandardCharsets.UTF_8);
        final int outLen = 32;

        try {
            // Extract
            Mac extractMac = Mac.getInstance(MAC_ALGORITHM);
            extractMac.init(new SecretKeySpec(finalSalt, MAC_ALGORITHM));
            byte[] prk = extractMac.doFinal(rootKey);

            // Expand
            Mac expandMac = Mac.getInstance(MAC_ALGORITHM);
            expandMac.init(new SecretKeySpec(prk, MAC_ALGORITHM));

            byte[] ret = new byte[outLen];
            byte[] t = new byte[0];
            int copied = 0;
            for (int counter = 1; copied < outLen; counter++) {
                expandMac.reset();
                expandMac.update(t);
                expandMac.update(finalPurpose);
                expandMac.update((byte) counter);
                t = expandMac.doFinal();
                int n = Math.min(t.length, outLen - copied);
                System.arraycopy(t, 0, ret, copied, n);
                copied += n;
            }
            return ret;
        } catch (Exception e) {
            // 异常消息仅含类型，不含密钥材料
            throw new IllegalStateException("HKDF 派生失败 (" + e.getClass().getSimpleName() + ")", e);
        }
    }

    public static byte[] utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @return SHA-256 摘要的十六进制小写串（指纹口径，canary 元数据表使用）
     */
    public static String sha256Hex(byte[] input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input);
            char[] hex = new char[digest.length * 2];
            for (int i = 0; i < digest.length; i++) {
                int v = digest[i] & 0xFF;
                hex[i * 2] = HEX_CHARS[v >>> 4];
                hex[i * 2 + 1] = HEX_CHARS[v & 0x0F];
            }
            return new String(hex);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败 (" + e.getClass().getSimpleName() + ")", e);
        }
    }
}
