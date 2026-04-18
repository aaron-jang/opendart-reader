package io.github.aaronjang.opendart.api

import io.github.aaronjang.opendart.internal.DartClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DartListTest {

    private fun mockClientWithResponse(jsonResponse: String): DartClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(jsonResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return DartClient("test_key", mockEngine)
    }

    @Test
    fun `list returns disclosures from single page response`() = runTest {
        val json = """{"status":"000","message":"정상","page_no":1,"page_count":100,"total_count":1,"total_page":1,"list":[{"corp_cls":"Y","corp_name":"삼성전자","corp_code":"00126380","stock_code":"005930","rcept_no":"20240101000001","report_nm":"사업보고서","rcept_dt":"20240101","flr_nm":"삼성전자","rm":""}]}"""
        val client = mockClientWithResponse(json)
        val result = DartList.list(client, corpCode = "00126380")
        assertEquals(1, result.size)
        assertEquals("삼성전자", result[0].corpName)
        assertEquals("20240101000001", result[0].rceptNo)
        client.close()
    }

    @Test
    fun `list returns empty list when no results`() = runTest {
        val json = """{"status":"013","message":"조회된 데이터가 없습니다."}"""
        val client = mockClientWithResponse(json)
        val result = DartList.list(client, corpCode = "00126380")
        assertEquals(0, result.size)
        client.close()
    }
}
