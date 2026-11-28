package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;

import java.util.Base64;

/**
 * 根密钥解码唯一入口：启动守卫与运行期加载必须走同一条路径，
 * 避免「守卫 trim 通过、运行期不 trim 解码失败 / 派生出另一把 key」这类不一致
 *
 * @author Uncarbon
 */
public final class RootKeyCodec {

    public static final int ROOT_KEY_LENGTH = 32;

    private RootKeyCodec() {
    }

    /**
     * 解码并校验为 32 字节根密钥
     *
     * @param base64Value 配置值
     * @param name        配置项名（仅用于报错，不暴露密钥内容）
     * @throws IllegalStateException 缺失 / 含空白 / 非 Base64 / 长度不符
     */
    public static byte[] decode(String base64Value, String name) {
        if (base64Value == null || base64Value.isBlank()) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " 密钥配置 " + name + " 缺失");
        }
        // 显式拒绝而非静默 trim：密钥值不应由配置里的空白决定，YAML 折行/尾随空格一律要求修正配置
        if (!base64Value.equals(base64Value.strip())) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " 密钥配置 " + name + " 首尾含空白字符，请修正配置");
        }
        for (int i = 0; i < base64Value.length(); i++) {
            if (Character.isWhitespace(base64Value.charAt(i))) {
                throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " 密钥配置 " + name + " 内部含空白字符，请修正配置");
            }
        }
        byte[] key;
        try {
            key = Base64.getDecoder().decode(base64Value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " 密钥配置 " + name + " 不是合法 Base64");
        }
        if (key.length != ROOT_KEY_LENGTH) {
            throw new IllegalStateException(
                    FieldCryptoConstant.LOG_PREFIX + " 密钥 " + name + " 须为 " + ROOT_KEY_LENGTH + " 字节，实际 " + key.length + " 字节");
        }
        return key;
    }

    /**
     * 可选密钥：空值返回 null，非空走 {@link #decode}
     */
    public static byte[] decodeOptional(String base64Value, String name) {
        if (base64Value == null || base64Value.isBlank()) {
            return null;
        }
        return decode(base64Value, name);
    }
}
