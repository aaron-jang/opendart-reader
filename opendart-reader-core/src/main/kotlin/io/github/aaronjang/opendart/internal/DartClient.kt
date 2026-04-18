package io.github.aaronjang.opendart.internal

import io.github.aaronjang.opendart.exception.DartException
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.io.Closeable

class DartClient(
    private val apiKey: String,
    engine: HttpClientEngine = CIO.create(),
) : Closeable {

    companion object {
        const val API_BASE_URL = "https://opendart.fss.or.kr/api/"
        const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.3904.108 Safari/537.36"
    }

    private val json = Json { ignoreUnknownKeys = true }

    val httpClient = HttpClient(engine)

    suspend fun getJson(
        endpoint: String,
        params: Map<String, String>,
        checkStatus: Boolean = true,
    ): JsonObject {
        val response: HttpResponse = httpClient.get("$API_BASE_URL$endpoint") {
            parameter("crtfc_key", apiKey)
            params.forEach { (k, v) -> parameter(k, v) }
        }
        val body = response.bodyAsText()
        val jo = json.parseToJsonElement(body).jsonObject
        if (checkStatus) {
            val status = jo["status"]?.jsonPrimitive?.content
            if (status != null && status != "000") {
                val message = jo["message"]?.jsonPrimitive?.content ?: "Unknown error"
                throw DartException(status, message)
            }
        }
        return jo
    }

    suspend fun getBytes(endpoint: String, params: Map<String, String>): ByteArray {
        val response: HttpResponse = httpClient.get("$API_BASE_URL$endpoint") {
            parameter("crtfc_key", apiKey)
            params.forEach { (k, v) -> parameter(k, v) }
        }
        return response.readRawBytes()
    }

    override fun close() {
        httpClient.close()
    }
}
