package io.github.aaronjang.opendart.spring

import io.github.aaronjang.opendart.OpenDartReader
import io.github.aaronjang.opendart.cache.DartCache
import io.github.aaronjang.opendart.cache.InMemoryDartCache
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(OpenDartProperties::class)
@ConditionalOnProperty(prefix = "opendart", name = ["api-key"])
class OpenDartAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DartCache::class)
    @ConditionalOnBean(CacheManager::class)
    fun springDartCache(cacheManager: CacheManager): DartCache {
        return SpringDartCache(cacheManager)
    }

    @Bean
    @ConditionalOnMissingBean(DartCache::class)
    fun inMemoryDartCache(): DartCache {
        return InMemoryDartCache()
    }

    @Bean
    @ConditionalOnMissingBean
    fun openDartReader(properties: OpenDartProperties, dartCache: DartCache): OpenDartReader {
        return OpenDartReader.createSync(properties.apiKey, dartCache)
    }
}
