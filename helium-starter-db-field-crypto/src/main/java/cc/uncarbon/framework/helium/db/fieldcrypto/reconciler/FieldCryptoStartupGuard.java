package cc.uncarbon.framework.helium.db.fieldcrypto.reconciler;

import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.props.HeliumFieldCryptoProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.List;

/**
 * 字段加密启动守卫，不符合启动条件则拒绝启动
 *
 * <ol>
 *   <li>密钥配置缺失 / 非 32 字节 → 拒启（yml 中密钥引用不得带字面量默认值，缺失即失败）</li>
 *   <li>密钥非合法 Base64 → 拒启</li>
 *   <li>keyId 字符集不合法（限 [A-Za-z0-9_-]{1,16}，禁止含 '$'）→ 拒启</li>
 * </ol>
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
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " fail-closed: 密钥配置 " + name + " 缺失（禁止字面量默认值兜底）");
        }
        try {
            return Base64.getDecoder().decode(base64Value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " fail-closed: 密钥配置 " + name + " 不是合法 Base64");
        }
    }

    private static void require32Bytes(byte[] key, String name) {
        if (key.length != 32) {
            // 只暴露长度事实，不暴露内容
            throw new IllegalStateException(
                    FieldCryptoConstant.LOG_PREFIX + " fail-closed: 密钥 " + name + " 须为 32 字节（256bit），实际 " + key.length + " 字节");
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
                        FieldCryptoConstant.LOG_PREFIX + " fail-closed: keyId 字符集不合法: " + keyId);
            }
        }

        byte[] dataRootKey = decodeKey(props.getDataRootKey(), "data-root-key");
        byte[] indexRootKey = decodeKey(props.getIndexRootKey(), "index-root-key");
        require32Bytes(dataRootKey, "data-root-key");
        require32Bytes(indexRootKey, "index-root-key");

        log.info(FieldCryptoConstant.LOG_PREFIX + " >> fail-closed 启动校验通过, activeKeyId={}, keyIdCount={}",
                props.getActiveDataKeyId(), keyIds.size());
    }
}
