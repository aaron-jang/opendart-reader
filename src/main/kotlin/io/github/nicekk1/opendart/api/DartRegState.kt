package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.*
import kotlinx.serialization.json.*
import java.time.LocalDate

object DartRegState {
    suspend fun regstate(
        client: DartClient, corpCode: String, keyWord: String,
        start: LocalDate? = null, end: LocalDate? = null,
    ): List<Map<String, String>> {
        val endpoint = REGSTATE_KEYWORD_MAP[keyWord]
            ?: throw IllegalArgumentException("Invalid key_word: '$keyWord'. Use one of: ${REGSTATE_KEYWORD_MAP.keys}")
        val jo = try {
            client.getJson("$endpoint.json", mapOf(
                "corp_code" to corpCode,
                "bgn_de" to formatDate(start ?: defaultStart()),
                "end_de" to formatDate(end ?: defaultEnd()),
            ))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) { return emptyList() }

        val list = jo["list"]?.jsonArray
        if (list != null) {
            return list.map { item -> item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") } }
        }
        val group = jo["group"]?.jsonArray
        if (group != null) {
            val result = mutableListOf<Map<String, String>>()
            for (g in group) {
                val title = g.jsonObject["title"]?.jsonPrimitive?.contentOrNull ?: ""
                val innerList = g.jsonObject["list"]?.jsonArray ?: continue
                for (item in innerList) {
                    val map = item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }.toMutableMap()
                    map["title"] = title
                    result.add(map)
                }
            }
            return result
        }
        return emptyList()
    }
}
