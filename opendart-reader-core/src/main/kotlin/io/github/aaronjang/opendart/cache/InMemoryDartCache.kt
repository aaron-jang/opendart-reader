package io.github.aaronjang.opendart.cache

import java.util.concurrent.ConcurrentHashMap

/**
 * 인메모리 기반 기본 캐시 구현체.
 * 파일 시스템을 사용하지 않고 메모리에만 저장합니다.
 * 애플리케이션 재시작 시 캐시가 초기화됩니다.
 */
class InMemoryDartCache : DartCache {
    private val store = ConcurrentHashMap<String, String>()

    override fun get(key: String): String? = store[key]

    override fun put(key: String, value: String) {
        store[key] = value
    }

    override fun containsKey(key: String): Boolean = store.containsKey(key)
}
