package cc.uncarbon.framework.helium.db.fieldcrypto.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Helium 字段加密配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.DB_FIELD_CRYPTO)
@Data
public class HeliumFieldCryptoProperties {

    /**
     * 数据加密根密钥 dataRootKey；32字节，可通过 openssl rand -base64 32 生成
     */
    private String dataRootKey;

    /**
     * 可用 keyId 集合（新旧并存支持零停机轮换）
     *
     * <p>轮换：新增 keyId后，修改 active-data-key-id；业务低峰期，通过异步任务，扫描旧前缀数据->解密->重加密->原地更新
     * <p>旧前缀数据数量 = 0，才可移除旧 keyId
     */
    private List<String> dataKeyIds = new ArrayList<>();

    /**
     * 加密使用的活跃 keyId，必须存在于 dataKeyIds
     */
    private String activeDataKeyId;

    /**
     * 索引根密钥 indexRootKey；32字节，可通过 openssl rand -base64 32 生成
     */
    private String indexRootKey;

    /**
     * 根密钥双指纹 canary
     */
    private Canary canary = new Canary();

    public boolean isConfigured() {
        return dataRootKey != null && !dataRootKey.isBlank()
                && activeDataKeyId != null && !activeDataKeyId.isBlank()
                && dataKeyIds != null && dataKeyIds.contains(activeDataKeyId)
                && indexRootKey != null && !indexRootKey.isBlank();
    }

    @Data
    public static class Canary {

        /**
         * 是否启用启动期指纹比对
         * <P>默认开启，用于启动时对账
         */
        private boolean enabled = true;

        /**
         * 轮换覆写维护开关，仅轮换期间临时设为 true 使用
         * <P>允许首节点覆写 canary 指纹；无开关时不匹配一律拒启
         */
        private boolean allowOverwrite = false;
    }
}
