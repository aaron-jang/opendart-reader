package io.github.aaronjang.opendart.api

import io.github.aaronjang.opendart.internal.DartClient
import io.github.aaronjang.opendart.model.FinancialStatement
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.nio.file.Path

object DartFinState {

    private val json = Json { ignoreUnknownKeys = true }

    private val REPRT_CODE_MAP = mapOf(
        "11013" to "1분기보고서", "11012" to "반기보고서",
        "11014" to "3분기보고서", "11011" to "사업보고서",
    )
    private val FS_DIV_MAP = mapOf("CFS" to "연결재무제표", "OFS" to "별도(개별)재무제표")

    suspend fun finstate(
        client: DartClient, corpCode: String, bsnsYear: Int, reprtCode: String = "11011",
    ): List<FinancialStatement> {
        val isMulti = "," in corpCode
        val endpoint = if (isMulti) "fnlttMultiAcnt.json" else "fnlttSinglAcnt.json"
        val jo = try {
            client.getJson(endpoint, mapOf("corp_code" to corpCode, "bsns_year" to bsnsYear.toString(), "reprt_code" to reprtCode))
        } catch (e: io.github.aaronjang.opendart.exception.DartException) { return emptyList() }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { json.decodeFromJsonElement<FinancialStatement>(it) }
    }

    suspend fun finstateAll(
        client: DartClient, corpCode: String, bsnsYear: Int,
        reprtCode: String = "11011", fsDiv: String = "CFS",
    ): List<FinancialStatement> {
        require(reprtCode in REPRT_CODE_MAP) { "Invalid reprt_code. Use one of: $REPRT_CODE_MAP" }
        require(fsDiv in FS_DIV_MAP) { "Invalid fs_div. Use one of: $FS_DIV_MAP" }
        val jo = try {
            client.getJson("fnlttSinglAcntAll.json", mapOf(
                "corp_code" to corpCode, "bsns_year" to bsnsYear.toString(),
                "reprt_code" to reprtCode, "fs_div" to fsDiv,
            ))
        } catch (e: io.github.aaronjang.opendart.exception.DartException) { return emptyList() }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { json.decodeFromJsonElement<FinancialStatement>(it) }
    }

    suspend fun finstateXml(client: DartClient, rcpNo: String, savePath: Path): Boolean {
        val bytes = client.getBytes("fnlttXbrl.xml", mapOf("rcept_no" to rcpNo))
        Files.write(savePath, bytes)
        return true
    }

    suspend fun xbrlTaxonomy(client: DartClient, sjDiv: String): List<Map<String, String>> {
        val jo = try {
            client.getJson("xbrlTaxonomy.json", mapOf("sj_div" to sjDiv))
        } catch (e: io.github.aaronjang.opendart.exception.DartException) { return emptyList() }
        val list = jo["list"]?.jsonArray ?: return emptyList()
        return list.map { item -> item.jsonObject.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") } }
    }
}
