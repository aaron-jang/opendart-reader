package io.github.nicekk1.opendart

import io.github.nicekk1.opendart.model.CorpCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OpenDartReaderTest {

    private fun createTestReader(): OpenDartReader {
        return OpenDartReader.forTesting(
            apiKey = "test_key",
            corpCodes = listOf(
                CorpCode("00126380", "삼성전자", "005930"),
                CorpCode("00164779", "SK하이닉스", "000660"),
                CorpCode("00999999", "비상장회사", " "),
            )
        )
    }

    @Test
    fun `findCorpCode finds by stock code`() {
        val reader = createTestReader()
        assertEquals("00126380", reader.findCorpCode("005930"))
        reader.close()
    }

    @Test
    fun `findCorpCode finds by corp name`() {
        val reader = createTestReader()
        assertEquals("00126380", reader.findCorpCode("삼성전자"))
        reader.close()
    }

    @Test
    fun `findCorpCode finds by corp code`() {
        val reader = createTestReader()
        assertEquals("00126380", reader.findCorpCode("00126380"))
        reader.close()
    }

    @Test
    fun `findCorpCode returns null for unknown`() {
        val reader = createTestReader()
        assertNull(reader.findCorpCode("없는회사"))
        reader.close()
    }
}
