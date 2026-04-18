package io.github.aaronjang.opendart.internal

import io.github.aaronjang.opendart.model.CorpCode
import kotlin.test.Test
import kotlin.test.assertEquals

class CorpCodeCacheTest {

    @Test
    fun `parseCorpCodeXml parses XML correctly`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <result>
                <list>
                    <corp_code>00126380</corp_code>
                    <corp_name>삼성전자</corp_name>
                    <stock_code>005930</stock_code>
                    <modify_date>20230101</modify_date>
                </list>
                <list>
                    <corp_code>00164779</corp_code>
                    <corp_name>SK하이닉스</corp_name>
                    <stock_code>000660</stock_code>
                    <modify_date>20230101</modify_date>
                </list>
                <list>
                    <corp_code>99999999</corp_code>
                    <corp_name>비상장회사</corp_name>
                    <stock_code> </stock_code>
                    <modify_date>20230101</modify_date>
                </list>
            </result>
        """.trimIndent()

        val result = parseCorpCodeXml(xml)
        assertEquals(3, result.size)
        assertEquals("00126380", result[0].corpCode)
        assertEquals("삼성전자", result[0].corpName)
        assertEquals("005930", result[0].stockCode)
        assertEquals(" ", result[2].stockCode)
    }
}
