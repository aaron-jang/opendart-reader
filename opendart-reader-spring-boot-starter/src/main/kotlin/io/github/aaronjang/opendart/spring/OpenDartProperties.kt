package io.github.aaronjang.opendart.spring

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "opendart")
class OpenDartProperties {
    /** OpenDART API 인증키 */
    var apiKey: String = ""
}
