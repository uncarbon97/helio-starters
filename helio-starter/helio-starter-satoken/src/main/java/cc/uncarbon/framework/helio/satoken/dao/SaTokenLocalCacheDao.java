package cc.uncarbon.framework.helio.satoken.dao;

import cc.uncarbon.framework.helio.base.cache.TimedCacheEx;
import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * 在 SA-Token 读取时，本地缓存一定时长，减少对 Redis 的 IO
 * 在多实例部署时存在一定安全风险：用户登出时，A机器的缓存已清除，但B机器的缓存还认为有效
 * 所以请谨慎设置本地缓存有效时长
 *
 * @author Uncarbon
 * @since 2.1.0
 */
@Slf4j
public class SaTokenLocalCacheDao extends SaTokenDaoForRedisTemplate {

    private static final String LOG_PREFIX = "[SaTokenLocalCacheDao]";

    /**
     * 标记 key 为空值
     */
    protected final TimedCacheEx<String, Boolean> nullValueMarker;
    protected final TimedCacheEx<String, String> stringValueCache;
    protected final TimedCacheEx<String, Object> objectValueCache;


    /**
     * 创建本地缓存 SaTokenDao
     *
     * @param duration 本地缓存有效时长，单位=秒
     * @param capacity 本地缓存最大容量，注意：每次超过容量都会触发一次清理过程
     */
    public SaTokenLocalCacheDao(long duration, int capacity) {
        if (duration < 1) {
            throw new IllegalArgumentException("duration CANNOT less than 1");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity CANNOT less than 1");
        }

        long asMilliseconds = duration * 1000;

        this.nullValueMarker = new TimedCacheEx<>(asMilliseconds, capacity);
        this.stringValueCache = new TimedCacheEx<>(asMilliseconds, capacity);
        this.objectValueCache = new TimedCacheEx<>(asMilliseconds, capacity);
        // 默认开nullValueMarker的清理定时任务，避免长时间缓存空值
        enableNullValueMarkerPruneSchedule(400);
        // 不默认开stringValueCache、objectValueCache的清理定时任务

        log.info(LOG_PREFIX + " 已启用");
    }

    /**
     * 开 nullValueMarker 的清理定时任务
     *
     * @param delay 间隔时长，单位毫秒
     */
    public void enableNullValueMarkerPruneSchedule(long delay) {
        this.nullValueMarker.schedulePrune(delay);
    }

    /**
     * 关 nullValueMarker 的清理定时任务
     */
    public void disableNullValueMarkerPruneSchedule() {
        this.nullValueMarker.cancelPruneSchedule();
    }

    /**
     * 开 stringValueCache 的清理定时任务
     *
     * @param delay 间隔时长，单位毫秒
     */
    public void enableStringValueCachePruneSchedule(long delay) {
        this.stringValueCache.schedulePrune(delay);
    }

    /**
     * 关 stringValueCache 的清理定时任务
     */
    public void disableStringValueCachePruneSchedule() {
        this.stringValueCache.cancelPruneSchedule();
    }

    /**
     * 开 objectValueCache 的清理定时任务
     *
     * @param delay 间隔时长，单位毫秒
     */
    public void enableObjectValueCachePruneSchedule(long delay) {
        this.objectValueCache.schedulePrune(delay);
    }

    /**
     * 关 objectValueCache 的清理定时任务
     */
    public void disableObjectValueCachePruneSchedule() {
        this.objectValueCache.cancelPruneSchedule();
    }

    @Override
    public String get(String key) {
        if (nullValueMarker.containsKey(key)) {
            // 明确知道为空，直接返回
            return null;
        }
        String cacheValue = stringValueCache.get(key, false);
        if (cacheValue == null) {
            // 本地缓存为空，实时查询
            cacheValue = super.get(key);
        }
        // cache-aside
        if (cacheValue == null) {
            nullValueMarker.put(key, Boolean.TRUE);
        } else {
            stringValueCache.put(key, cacheValue);
        }
        return cacheValue;
    }

    @Override
    public void set(String key, String value, long timeout) {
        super.set(key, value, timeout);
        // cache-aside
        nullValueMarker.remove(key);
        stringValueCache.remove(key);
    }

    @Override
    public void update(String key, String value) {
        super.update(key, value);
        // cache-aside
        nullValueMarker.remove(key);
        stringValueCache.remove(key);
    }

    @Override
    public void delete(String key) {
        super.delete(key);
        // cache-aside
        nullValueMarker.remove(key);
        stringValueCache.remove(key);
    }

    @Override
    public Object getObject(String key) {
        if (nullValueMarker.containsKey(key)) {
            // 明确知道为空，直接返回
            return null;
        }
        Object cacheValue = objectValueCache.get(key, false);
        if (cacheValue == null) {
            // 本地缓存为空，实时查询
            cacheValue = super.getObject(key);
        }
        // cache-aside
        if (cacheValue == null) {
            nullValueMarker.put(key, Boolean.TRUE);
        } else {
            objectValueCache.put(key, cacheValue);
        }
        return cacheValue;
    }

    @Override
    public void setObject(String key, Object object, long timeout) {
        super.setObject(key, object, timeout);
        // cache-aside
        nullValueMarker.remove(key);
        objectValueCache.remove(key);
    }

    @Override
    public void updateObject(String key, Object object) {
        super.updateObject(key, object);
        // cache-aside
        nullValueMarker.remove(key);
        objectValueCache.remove(key);
    }

    @Override
    public void deleteObject(String key) {
        super.deleteObject(key);
        // cache-aside
        nullValueMarker.remove(key);
        objectValueCache.remove(key);
    }

}
