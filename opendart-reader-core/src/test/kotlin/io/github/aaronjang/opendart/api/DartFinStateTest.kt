package io.github.aaronjang.opendart.api

import io.github.aaronjang.opendart.internal.DartClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DartFinStateTest {
    @Test
    fun `finstate single company returns financial statements`() = runTest {
        val jsonResponse = """{"status":"000","message":"정상","list":[{"rcept_no":"20240101000001","corp_code":"00126380","corp_name":"삼성전자","stock_code":"005930","reprt_code":"11011","bsns_year":"2023","fs_div":"CFS","fs_nm":"연결재무제표","sj_div":"BS","sj_nm":"재무상태표","account_nm":"자산총계","thstrm_nm":"제55기","thstrm_amount":"100000","frmtrm_nm":"제54기","frmtrm_amount":"90000","bfefrmtrm_nm":"제53기","bfefrmtrm_amount":"80000","ord":"1","currency":"KRW"}]}"""
        val mockEngine = MockEngine { _ ->
            respond(content = ByteReadChannel(jsonResponse), status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = DartClient("test_key", mockEngine)
        val result = DartFinState.finstate(client, "00126380", 2023)
        assertEquals(1, result.size)
        assertEquals("삼성전자", result[0].corpName)
        assertEquals("자산총계", result[0].accountNm)
        client.close()
    }

    @Test
    fun `finstate returns empty list when no data`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(content = ByteReadChannel("""{"status":"013","message":"조회된 데이터가 없습니다."}"""),
                status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = DartClient("test_key", mockEngine)
        val result = DartFinState.finstate(client, "00126380", 2010)
        assertEquals(0, result.size)
        client.close()
    }
}
