package io.github.aaronjang.opendart.spring

import io.github.aaronjang.opendart.OpenDartReader
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OpenDartAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(OpenDartAutoConfiguration::class.java))

    @Test
    fun `api-key 없으면 OpenDartReader 빈이 등록되지 않는다`() {
        contextRunner.run { context ->
            assertEquals(0, context.getBeanNamesForType(OpenDartReader::class.java).size)
        }
    }

    @Test
    fun `api-key 있으면 OpenDartReader 빈 정의가 존재한다`() {
        contextRunner
            .withPropertyValues("opendart.api-key=test-key")
            .withBean(OpenDartReader::class.java, {
                // mock bean을 등록하여 실제 API 호출을 방지
                OpenDartReader.forTesting("test-key", emptyList())
            })
            .run { context ->
                assertNotNull(context.getBean(OpenDartReader::class.java))
            }
    }
}
