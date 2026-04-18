package io.github.aaronjang.opendart.api

import io.github.aaronjang.opendart.internal.DartClient
import io.github.aaronjang.opendart.internal.defaultEnd
import io.github.aaronjang.opendart.internal.defaultStart
import io.github.aaronjang.opendart.internal.formatDate
import io.github.aaronjang.opendart.model.Company
import io.github.aaronjang.opendart.model.Disclosure
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream

object DartList {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun list(
        client: DartClient,
        corpCode: String = "",
        start: LocalDate? = null,
        end: LocalDate? = null,
        kind: String = "",
        kindDetail: String = "",
        final: Boolean = true,
    ): List<Disclosure> {
        val params = mutableMapOf(
            "corp_code" to corpCode,
            "bgn_de" to formatDate(start ?: defaultStart()),
            "end_de" to formatDate(end ?: defaultEnd()),
            "last_reprt_at" to if (final) "Y" else "N",
            "page_no" to "1",
            "page_count" to "100",
        )
        if (kind.isNotEmpty()) params["pblntf_ty"] = kind
        if (kindDetail.isNotEmpty()) params["pblntf_detail_ty"] = kindDetail

        val jo = try {
            client.getJson("list.json", params)
        } catch (e: io.github.aaronjang.opendart.exception.DartException) {
            if (e.status == "013") return emptyList()
            throw e
        }

        val listArray = jo["list"]?.jsonArray ?: return emptyList()
        val result = listArray.map { json.decodeFromJsonElement<Disclosure>(it) }.toMutableList()

        val totalPage = jo["total_page"]?.jsonPrimitive?.int ?: 1
        for (page in 2..totalPage) {
            delay(100)
            params["page_no"] = page.toString()
            val pageJo = client.getJson("list.json", params)
            val pageList = pageJo["list"]?.jsonArray ?: break
            result.addAll(pageList.map { json.decodeFromJsonElement<Disclosure>(it) })
        }
        return result
    }

    suspend fun company(client: DartClient, corpCode: String): Company {
        val jo = client.getJson("company.json", mapOf("corp_code" to corpCode))
        return json.decodeFromJsonElement(jo)
    }

    suspend fun companyByName(client: DartClient, corpCodeList: List<String>): List<Company> {
        return corpCodeList.map { corpCode -> company(client, corpCode) }
    }

    suspend fun document(client: DartClient, rcpNo: String): String {
        val bytes = client.getBytes("document.xml", mapOf("rcept_no" to rcpNo))
        val xmlBytes = extractFirstFromZip(bytes)
        return decodeXml(xmlBytes)
    }

    suspend fun documentAll(client: DartClient, rcpNo: String): List<String> {
        val bytes = client.getBytes("document.xml", mapOf("rcept_no" to rcpNo))
        return extractAllFromZip(bytes).map { decodeXml(it) }
    }

    private fun extractFirstFromZip(bytes: ByteArray): ByteArray {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            val entries = mutableListOf<Pair<String, ByteArray>>()
            var entry = zis.nextEntry
            while (entry != null) {
                entries.add(entry.name to zis.readBytes())
                entry = zis.nextEntry
            }
            entries.sortBy { it.first }
            return entries.firstOrNull()?.second ?: throw IllegalStateException("Empty zip file")
        }
    }

    private fun extractAllFromZip(bytes: ByteArray): List<ByteArray> {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            val entries = mutableListOf<Pair<String, ByteArray>>()
            var entry = zis.nextEntry
            while (entry != null) {
                entries.add(entry.name to zis.readBytes())
                entry = zis.nextEntry
            }
            entries.sortBy { it.first }
            return entries.map { it.second }
        }
    }

    private fun decodeXml(data: ByteArray): String {
        return try {
            data.toString(java.nio.charset.Charset.forName("EUC-KR"))
        } catch (e: Exception) {
            try {
                data.toString(Charsets.UTF_8)
            } catch (e2: Exception) {
                String(data)
            }
        }
    }
}
