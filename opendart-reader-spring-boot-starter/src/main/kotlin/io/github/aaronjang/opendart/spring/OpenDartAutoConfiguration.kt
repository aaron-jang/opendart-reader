package io.github.aaronjang.opendart.spring

import io.github.aaronjang.opendart.OpenDartReader
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(OpenDartProperties::class)
@ConditionalOnProperty(prefix = "opendart", name = ["api-key"])
class OpenDartAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun openDartReader(properties: OpenDartProperties): OpenDartReader {
        return OpenDartReader.createSync(properties.apiKey)
    }
}
