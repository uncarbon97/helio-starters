package cc.uncarbon.framework.redis.model;

import cc.uncarbon.framework.redis.enums.KeyTypeEnum;

/**
 * 已固定好键名的 Redis Key
 * @param key 已固定好的键名
 * @param keyType 键类型
 * @param valueClass 值类型
 * @param durationSeconds 有效时长，永久为-1，可通过 hasExpiration() 方法判断
 * @param <V>
 */
public record FormattedRedisKey<V>(
        String key,
        KeyTypeEnum keyType,
        Class<V> valueClass,
        long durationSeconds
) {
    /**
     * 是否已设置有效时长
     */
    public boolean hasExpiration() {
        return this.durationSeconds > 0;
    }
}
