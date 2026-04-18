package io.github.aaronjang.opendart.api

import io.github.aaronjang.opendart.internal.*
import kotlinx.serialization.json.*
import java.time.LocalDate

object DartEvent {
    suspend fun event(
        client: DartClient, corpCode: String, keyWord: String,
        start: LocalDate? = null, end: LocalDate? = null,
    ): List<Map<String, String>> {
        val endpoint = EVENT_KEYWORD_MAP[keyWord]
            ?: throw IllegalArgumentException("Invalid key_word: '$keyWord'. Use one of: ${EVENT_KEYWORD_MAP.keys}")
        val jo = try {
            client.getJson("$endpoint.json", mapOf(
                "corp_code" to corpCode,
                "bgn_de" to formatDate(start ?: defaultStart()),
                "end_de" to formatDate(end ?: defaultEnd()),
            ))
        } catch (e: io.github.aaronjang.opendart.exception.DartException) { return emptyList() }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item -> item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") } }
    }
}
