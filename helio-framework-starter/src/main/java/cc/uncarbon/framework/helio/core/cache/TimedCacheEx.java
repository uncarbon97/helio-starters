package cc.uncarbon.framework.helio.core.cache;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.lang.mutable.MutableObj;

/**
 * 继承自 hutool TimedCache
 * 增加了容量限制，重写了 containsKey 方法判断逻辑提升效率并支持空值
 * @param <K> key类型
 * @param <V> value类型
 *
 * @since 2.1.0
 * @author Uncarbon
 */
public class TimedCacheEx<K, V> extends TimedCache<K, V> {

    public TimedCacheEx(long timeout, int capacity) {
        super(timeout);
        this.capacity = capacity;
    }

    @Override
    public boolean containsKey(K key) {
        return this.cacheMap.containsKey(MutableObj.of(key));
    }
}
