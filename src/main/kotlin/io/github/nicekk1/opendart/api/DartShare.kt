package io.github.nicekk1.opendart.api

import io.github.nicekk1.opendart.internal.DartClient
import kotlinx.serialization.json.*

object DartShare {
    suspend fun majorShareholders(client: DartClient, corpCode: String): List<Map<String, String>> {
        val jo = try { client.getJson("majorstock.json", mapOf("corp_code" to corpCode))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) { return emptyList() }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item -> item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") } }
    }

    suspend fun majorShareholdersExec(client: DartClient, corpCode: String): List<Map<String, String>> {
        val jo = try { client.getJson("elestock.json", mapOf("corp_code" to corpCode))
        } catch (e: io.github.nicekk1.opendart.exception.DartException) { return emptyList() }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item -> item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") } }
    }
}
