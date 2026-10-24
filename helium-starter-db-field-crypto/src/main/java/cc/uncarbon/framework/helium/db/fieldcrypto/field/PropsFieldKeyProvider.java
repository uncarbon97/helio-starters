package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import cc.uncarbon.framework.helium.db.fieldcrypto.props.HeliumFieldCryptoProperties;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Base64;
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
        return Base64.getDecoder().decode(props.getDataRootKey());
    }

    @Override
    public List<String> loadDataKeyIds() {
        return new ArrayList<>(props.getDataKeyIds());
    }

    @Override
    public String loadActiveDataKeyId() {
        return props.getActiveDataKeyId();
    }

    @Override
    public byte[] loadIndexRootKey() {
        return Base64.getDecoder().decode(props.getIndexRootKey());
    }

    @Override
    public byte[] loadPreviousDataRootKey() {
        return props.getPreviousDataRootKey() == null || props.getPreviousDataRootKey().isBlank()
                ? null : Base64.getDecoder().decode(props.getPreviousDataRootKey().trim());
    }

    @Override
    public List<String> loadPreviousDataKeyIds() {
        return new ArrayList<>(props.getPreviousDataKeyIds());
    }
}
