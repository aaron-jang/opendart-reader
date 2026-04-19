package io.github.aaronjang.opendart.cache

/**
 * OpenDartReader 캐시 인터페이스.
 * 기업코드, 웹 스크래핑 결과 등을 캐싱합니다.
 */
interface DartCache {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun containsKey(key: String): Boolean
}
