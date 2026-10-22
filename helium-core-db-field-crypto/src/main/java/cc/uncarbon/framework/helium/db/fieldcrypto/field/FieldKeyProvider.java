package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import java.util.Collection;

/**
 * 字段加密密钥提供方 SPI
 *
 * <p>密钥体系为「根密钥 + HKDF 派生」：dataRootKey 按各 keyId 派生 data-key（密文带 {keyId}$ 前缀路由），
 * indexRootKey 按用途派生盲索引 index-key。框架自带配置文件实现；生产环境可实现本接口对接 KMS。</p>
 *
 * <p>实现方注意：根密钥须为 32 字节，返回数组会被防御性拷贝；密钥材料严禁出现在日志、异常消息、toString 中</p>
 *
 * @author Uncarbon
 */
public interface FieldKeyProvider {

    /**
     * @return 数据加密根密钥原始字节（32 字节），各代 data-key 由框架派生
     */
    byte[] loadDataRootKey();

    /**
     * @return 可用的 keyId 集合（新旧并存支持零停机轮换）
     */
    Collection<String> loadDataKeyIds();

    /**
     * @return 加密使用的活跃 keyId，必须在 {@link #loadDataKeyIds()} 内
     */
    String loadActiveDataKeyId();

    /**
     * @return 索引根密钥原始字节（32 字节）
     * <P>轮换代价高（需停写重建全量索引列），通常长周期使用
     */
    byte[] loadIndexRootKey();
}
