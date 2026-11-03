package cc.uncarbon.framework.helium.db.fieldcrypto.reconciler;

import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.RootKeyCodec;
import cc.uncarbon.framework.helium.db.fieldcrypto.props.HeliumFieldCryptoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 字段加密启动守卫，不符合启动条件则拒绝启动
 *
 * @author Uncarbon
 */
@Slf4j
@RequiredArgsConstructor
public class FieldCryptoStartupGuard {

    private final HeliumFieldCryptoProperties props;


    /**
     * 校验失败抛 {@link IllegalStateException}，阻止 Spring 上下文启动
     */
    public void check() {
        if (!props.isConfigured()) {
            return;
        }

        List<String> keyIds = props.getDataKeyIds();
        if (keyIds == null || keyIds.isEmpty()) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " data-key-ids 至少需要配置一个 keyId");
        }
        for (String keyId : keyIds) {
            if (keyId == null || !FieldCryptoConstant.KEY_ID_PATTERN.matcher(keyId).matches()) {
                throw new IllegalStateException(
                        FieldCryptoConstant.LOG_PREFIX + " keyId 字符集不合法: " + keyId);
            }
        }

        RootKeyCodec.decode(props.getDataRootKey(), FieldCryptoConstant.DATA_ROOT_KEY_KEBAB);
        RootKeyCodec.decode(props.getIndexRootKey(), FieldCryptoConstant.INDEX_ROOT_KEY_KEBAB);
        checkPreviousGeneration(keyIds);

        log.info(FieldCryptoConstant.LOG_PREFIX + " >> 启动校验通过, activeKeyId={}, keyIdCount={}",
                props.getActiveDataKeyId(), keyIds.size());
    }

    /**
     * 上一代根密钥配置校验：成对出现、长度、字符集、与当前代 keyId 无交集
     */
    private void checkPreviousGeneration(List<String> keyIds) {
        String previousDataRootKey = props.getPreviousDataRootKey();
        List<String> previousKeyIds = props.getPreviousDataKeyIds();
        boolean hasPrevRoot = previousDataRootKey != null && !previousDataRootKey.isBlank();
        boolean hasPrevIds = previousKeyIds != null && !previousKeyIds.isEmpty();
        if (!hasPrevRoot && !hasPrevIds) {
            return;
        }
        if (hasPrevRoot != hasPrevIds) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX
                    + FieldCryptoConstant.PREVIOUS_DATA_ROOT_KEY_KEBAB
                    + " 与 previous-data-key-ids 必须成对配置");
        }

        for (String keyId : previousKeyIds) {
            if (keyId == null || !FieldCryptoConstant.KEY_ID_PATTERN.matcher(keyId).matches()) {
                throw new IllegalStateException(
                        FieldCryptoConstant.LOG_PREFIX + " 上一代 keyId 字符集不合法: " + keyId);
            }
            if (keyIds.contains(keyId)) {
                throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX
                        + " keyId 同时存在于当前代与上一代集合: " + keyId);
            }
        }

        RootKeyCodec.decode(previousDataRootKey, FieldCryptoConstant.PREVIOUS_DATA_ROOT_KEY_KEBAB);
    }
}
