package io.github.aaronjang.opendart.api

import io.github.aaronjang.opendart.internal.DartClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DartReportTest {
    @Test
    fun `report returns list of maps`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"000","message":"정상","list":[{"se":"배당","thstrm":"1000"}]}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("test_key", mockEngine)
        val result = DartReport.report(client, "00126380", "배당", 2023)
        assertEquals(1, result.size)
        assertEquals("배당", result[0]["se"])
        assertEquals("1000", result[0]["thstrm"])
        client.close()
    }

    @Test
    fun `report throws on invalid keyword`() = runTest {
        val mockEngine = MockEngine { _ -> respond("") }
        val client = DartClient("test_key", mockEngine)
        assertFailsWith<IllegalArgumentException> {
            DartReport.report(client, "00126380", "없는키워드", 2023)
        }
        client.close()
    }
}
