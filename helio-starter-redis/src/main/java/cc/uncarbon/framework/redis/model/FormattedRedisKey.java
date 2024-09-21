package cc.uncarbon.framework.redis.model;

import cc.uncarbon.framework.redis.enums.KeyTypeEnum;
import cn.hutool.core.util.RandomUtil;

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

    /**
     * 计算随机有效时长，有助于缓解缓存击穿、缓存雪崩
     *
     * @param minRatio 最小比例因子，如0.9表示90%
     * @param maxRatio 最大比例因子，如1.1表示110%
     * @return 随机有效时长，单位=秒
     */
    public long randomDurationSeconds(double minRatio, double maxRatio) {
        if (!hasExpiration()) {
            throw new IllegalArgumentException("no-expiration key CANNOT calculate randomDurationSeconds");
        }
        return (long)(RandomUtil.randomDouble(this.durationSeconds * minRatio, this.durationSeconds * maxRatio));
    }
}
