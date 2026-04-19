package io.github.aaronjang.opendart.spring

import io.github.aaronjang.opendart.cache.DartCache
import org.springframework.cache.CacheManager

/**
 * Spring Cache 기반 DartCache 구현체.
 * Spring의 CacheManager를 통해 캐싱합니다. (Caffeine, Redis 등 사용 가능)
 */
class SpringDartCache(
    cacheManager: CacheManager,
    cacheName: String = "opendart",
) : DartCache {

    private val cache = cacheManager.getCache(cacheName)
        ?: throw IllegalStateException("Cache '$cacheName'을(를) 찾을 수 없습니다. CacheManager 설정을 확인하세요.")

    override fun get(key: String): String? = cache.get(key, String::class.java)

    override fun put(key: String, value: String) {
        cache.put(key, value)
    }

    override fun containsKey(key: String): Boolean = cache.get(key) != null
}
