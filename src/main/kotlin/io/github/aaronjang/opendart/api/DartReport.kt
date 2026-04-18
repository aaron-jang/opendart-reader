package io.github.aaronjang.opendart.api

import io.github.aaronjang.opendart.internal.DartClient
import io.github.aaronjang.opendart.internal.REPORT_KEYWORD_MAP
import kotlinx.serialization.json.*

object DartReport {
    suspend fun report(
        client: DartClient, corpCode: String, keyWord: String,
        bsnsYear: Int, reprtCode: String = "11011",
    ): List<Map<String, String>> {
        val endpoint = REPORT_KEYWORD_MAP[keyWord]
            ?: throw IllegalArgumentException("Invalid key_word: '$keyWord'. Use one of: ${REPORT_KEYWORD_MAP.keys}")
        val jo = try {
            client.getJson("$endpoint.json", mapOf(
                "corp_code" to corpCode, "bsns_year" to bsnsYear.toString(), "reprt_code" to reprtCode,
            ))
        } catch (e: io.github.aaronjang.opendart.exception.DartException) { return emptyList() }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item -> item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") } }
    }
}
