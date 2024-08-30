package cc.uncarbon.framework.redis.model;

import cc.uncarbon.framework.redis.enums.KeyTypeEnum;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 统一 Redis Key 定义，方便集中管理
 * @param <V> 值类型
 */
@Getter
public class RedisKeyDefinition<V> {

    /**
     * 键名模板，占位符需要用 {}
     */
    private final String keyTemplate;

    /**
     * 键类型
     */
    private final KeyTypeEnum keyType;

    /**
     * 值类型对应 Class
     */
    private final Class<V> valueClass;

    /**
     * 有效时长，单位=秒
     */
    private final long durationSeconds;


    /**
     * 不设置有效时长
     * @param keyTemplate 键名模板，占位符需要用 {}
     * @param keyType 键类型
     * @param valueClass 值类型对应 Class
     */
    public RedisKeyDefinition(String keyTemplate, KeyTypeEnum keyType, Class<V> valueClass) {
        this.keyTemplate = keyTemplate;
        this.keyType = keyType;
        this.valueClass = valueClass;
        this.durationSeconds = -1;
    }

    /**
     * 有设置有效时长
     * @param keyTemplate 键名模板，占位符需要用 {}
     * @param keyType 键类型
     * @param valueClass 值类型对应 Class
     * @param duration 有效时长
     * @param timeUnit 有效时长的单位，最低为秒
     */
    public RedisKeyDefinition(String keyTemplate, KeyTypeEnum keyType, Class<V> valueClass, long duration, TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.NANOSECONDS || timeUnit == TimeUnit.MICROSECONDS || timeUnit == TimeUnit.MILLISECONDS) {
            throw new IllegalArgumentException("timeUnit CANNOT be NANOSECONDS or MICROSECONDS or MILLISECONDS");
        }

        this.keyTemplate = keyTemplate;
        this.keyType = keyType;
        this.valueClass = valueClass;

        // 兜底：最短缓存1s
        long asSeconds = timeUnit.toSeconds(duration);
        if (asSeconds < 1) {
            asSeconds = 1;
        }
        this.durationSeconds = asSeconds;
    }

    /**
     * 固定一个 Redis Key 的键名
     * @param params 键名模板占位符填充参数
     */
    public FormattedRedisKey<V> format(Object... params) {
        return new FormattedRedisKey<>(CharSequenceUtil.format(this.keyTemplate, params),
                this.keyType, this.valueClass, this.durationSeconds);
    }
}
