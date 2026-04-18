package io.github.aaronjang.opendart.api

import io.github.aaronjang.opendart.exception.DartException
import io.github.aaronjang.opendart.internal.DartClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DartClientTest {

    @Test
    fun `getJson returns parsed json when status 000`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"000","message":"정상","list":[{"name":"test"}]}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("test_key", mockEngine)
        val result = client.getJson("list.json", mapOf())
        assertEquals("000", result["status"]?.jsonPrimitive?.content)
        client.close()
    }

    @Test
    fun `getJson throws DartException when status not 000`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"status":"010","message":"등록되지 않은 인증키"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = DartClient("bad_key", mockEngine)
        assertFailsWith<DartException> {
            client.getJson("list.json", mapOf())
        }
        client.close()
    }
}
