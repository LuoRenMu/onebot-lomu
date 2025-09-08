package cn.luorenmu.common.utils

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.toJSONString
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.logging.log4j.util.Strings
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * 基于 Caffeine 的本地缓存工具类
 * @author LoMu
 * Date 2025.01.28 16:53
 */
@Component
class CaffeineUtils {

    private val log = KotlinLogging.logger {}

    // 使用 Caffeine 构建一个支持过期策略的缓存
    private val cache = Caffeine.newBuilder()
        .maximumSize(1000) // 最多缓存 1000 个条目
        .expireAfterWrite(30, TimeUnit.SECONDS) // 默认写入后过期
        .build<String, String>()

    /**
     * 删除缓存
     */
    fun deleteCache(key: String) {
        cache.invalidate(key)
    }

    /**
     * 当缓存不存在时，则 set（类似 Redis 的 setIfAbsent）
     *
     * @param key 缓存的 key
     * @param value 被缓存的值
     * @param timeout 过期时间
     * @param timeUnit 过期时间的单位
     * @return 是否 set（true = 之前不存在，成功设置）
     */
    fun <T> setCacheIfAbsent(key: String, value: T, timeout: Long = 1L, timeUnit: TimeUnit = TimeUnit.DAYS): Boolean {
        val json = value.toJSONString() ?: return false
        return cache.asMap().putIfAbsent(key, json) == null
    }

    /**
     * 设置缓存
     *
     * @param key 缓存的 key
     * @param value 被缓存的值
     * @param timeout 过期时间
     * @param timeUnit 过期时间的单位
     */
    fun <T> setCache(key: String, value: T, timeout: Long = 1L, timeUnit: TimeUnit = TimeUnit.DAYS) {
        val json = value.toJSONString() ?: return
        // Caffeine 不支持单个 key 设置不同过期时间（除非使用 Expiry 策略）
        // 所以这里统一使用默认过期策略
        cache.put(key, json)
    }

    /**
     * 获取缓存，如果不存在则通过 onMiss 加载并回填
     *
     * @param key 缓存的 key
     * @param valueType 返回值类型
     * @param onMiss 缓存未命中时的加载函数
     * @param timeout 过期时间（Caffeine 全局统一，此参数仅用于 setCache 时兼容）
     * @param timeUnit 过期时间单位
     * @param lock 本地同步锁对象（防止缓存穿透/击穿）
     * @return 缓存对象或 null
     */
    fun <T> getCache(
        key: String,
        valueType: Class<T>,
        onMiss: (() -> T)? = null,
        timeout: Long = 1L,
        timeUnit: TimeUnit = TimeUnit.DAYS,
        lock: Class<*> = CaffeineUtils::class.java,
    ): T? {
        return try {
            var json = cache.getIfPresent(key)
            if (Strings.isEmpty(json)) {
                if (onMiss == null) {
                    return null
                }
                // 上锁防止缓存击穿
                synchronized(lock) {
                    // 再次检查
                    json = cache.getIfPresent(key)
                    if (Strings.isEmpty(json)) {
                        val result = onMiss()
                        setCache(key, result, timeout, timeUnit)
                        return result
                    }
                }
            }
            JSON.parseObject(json, valueType)
        } catch (e: Exception) {
            log.error(e) { "CaffeineUtils getCache error for key: $key" }
            null
        }
    }
}