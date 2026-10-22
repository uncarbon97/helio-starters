package cc.uncarbon.framework.helium.db.fieldcrypto.reconciler;

import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.props.HeliumFieldCryptoProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.List;

/**
 * 字段加密启动守卫，不符合启动条件则拒绝启动
 *
 * @author Uncarbon
 */
@Slf4j
public class FieldCryptoStartupGuard {

    private final HeliumFieldCryptoProperties props;

    public FieldCryptoStartupGuard(HeliumFieldCryptoProperties props) {
        this.props = props;
    }

    private static byte[] decodeKey(String base64Value, String name) {
        if (base64Value == null || base64Value.isBlank()) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " 密钥配置 " + name + " 缺失");
        }
        try {
            return Base64.getDecoder().decode(base64Value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " 密钥配置 " + name + " 不是合法 Base64");
        }
    }

    private static void require32Bytes(byte[] key, String name) {
        if (key.length != 32) {
            // 只暴露长度事实，不暴露内容
            throw new IllegalStateException(
                    FieldCryptoConstant.LOG_PREFIX + " 密钥 " + name + " 须为 32 字节，实际 " + key.length + " 字节");
        }
    }

    /**
     * 校验失败抛 {@link IllegalStateException}，阻止 Spring 上下文启动
     */
    public void check() {
        if (!props.isConfigured()) {
            // 未启用字段加密（未配置 active-data-key-id 等前置条件时不会走到这里）
            return;
        }

        List<String> keyIds = props.getDataKeyIds();
        for (String keyId : keyIds) {
            if (keyId == null || !FieldCryptoConstant.KEY_ID_PATTERN.matcher(keyId).matches()) {
                throw new IllegalStateException(
                        FieldCryptoConstant.LOG_PREFIX + " keyId 字符集不合法: " + keyId);
            }
        }

        byte[] dataRootKey = decodeKey(props.getDataRootKey(), FieldCryptoConstant.DATA_ROOT_KEY_KEBAB);
        byte[] indexRootKey = decodeKey(props.getIndexRootKey(), FieldCryptoConstant.INDEX_ROOT_KEY_KEBAB);
        require32Bytes(dataRootKey, FieldCryptoConstant.DATA_ROOT_KEY_KEBAB);
        require32Bytes(indexRootKey, FieldCryptoConstant.INDEX_ROOT_KEY_KEBAB);

        log.info(FieldCryptoConstant.LOG_PREFIX + " >> 启动校验通过, activeKeyId={}, keyIdCount={}",
                props.getActiveDataKeyId(), keyIds.size());
    }
}
