/**
 * 作用：
 * 1️⃣使用 Jackson 作为 RedisTemplate 的 value-serializer
 * 2️⃣封装基于 Redisson 的Redis分布式可重入锁（`RedisDistributedLock` 以及对应的模板类 `RedisDistributedLockTemplate`）
 * 3️⃣提供`RedisKeyDefinition`统一 Redis Key 定义，方便集中管理
 */

package cc.uncarbon.framework.redis;
