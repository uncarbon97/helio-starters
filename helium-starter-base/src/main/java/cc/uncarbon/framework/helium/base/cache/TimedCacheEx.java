package cc.uncarbon.framework.helium.base.cache;

import cn.hutool.cache.impl.TimedCache;

/**
 * 继承自 hutool TimedCache，增加了容量限制
 * @param <K> key 类型
 * @param <V> value 类型
 *
 * @since 2.1.0
 * @author Uncarbon
 */
public class TimedCacheEx<K, V> extends TimedCache<K, V> {

    public TimedCacheEx(long timeout, int capacity) {
        super(timeout);
        this.capacity = capacity;
    }
}
