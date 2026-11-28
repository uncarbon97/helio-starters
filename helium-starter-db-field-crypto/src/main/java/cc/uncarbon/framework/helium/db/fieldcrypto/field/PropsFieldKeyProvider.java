package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.props.HeliumFieldCryptoProperties;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于配置文件的 {@link FieldKeyProvider} 实现
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class PropsFieldKeyProvider implements FieldKeyProvider {

    private final HeliumFieldCryptoProperties props;


    @Override
    public byte[] loadDataRootKey() {
        return RootKeyCodec.decode(props.getDataRootKey(), FieldCryptoConstant.DATA_ROOT_KEY_KEBAB);
    }

    @Override
    public List<String> loadDataKeyIds() {
        return copyOf(props.getDataKeyIds());
    }

    @Override
    public String loadActiveDataKeyId() {
        return props.getActiveDataKeyId();
    }

    @Override
    public byte[] loadIndexRootKey() {
        return RootKeyCodec.decode(props.getIndexRootKey(), FieldCryptoConstant.INDEX_ROOT_KEY_KEBAB);
    }

    @Override
    public byte[] loadPreviousDataRootKey() {
        return RootKeyCodec.decodeOptional(props.getPreviousDataRootKey(), FieldCryptoConstant.PREVIOUS_DATA_ROOT_KEY_KEBAB);
    }

    @Override
    public List<String> loadPreviousDataKeyIds() {
        return copyOf(props.getPreviousDataKeyIds());
    }

    /**
     * YAML 显式写 {@code key:} 空值会把 List 默认值覆盖为 null，这里统一兜底
     */
    private static List<String> copyOf(List<String> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}
