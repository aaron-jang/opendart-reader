package io.github.nicekk1.opendart

import io.github.nicekk1.opendart.api.*
import io.github.nicekk1.opendart.internal.CorpCodeCache
import io.github.nicekk1.opendart.internal.DartClient
import io.github.nicekk1.opendart.model.*
import io.github.nicekk1.opendart.scraping.DartScraper
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import java.io.Closeable
import java.nio.file.Path
import java.time.LocalDate

class OpenDartReader private constructor(
    private val client: DartClient,
    private val cache: CorpCodeCache,
) : Closeable {

    companion object {
        suspend fun create(apiKey: String, engine: HttpClientEngine = CIO.create()): OpenDartReader {
            val client = DartClient(apiKey, engine)
            val cache = CorpCodeCache(client)
            cache.load()
            return OpenDartReader(client, cache)
        }

        /** 테스트 전용 - 미리 로드된 기업코드로 인스턴스 생성 */
        fun forTesting(apiKey: String, corpCodes: List<CorpCode>): OpenDartReader {
            val client = DartClient(apiKey)
            val cache = CorpCodeCache(client)
            // Use reflection or a test-friendly approach to set corp codes
            val field = CorpCodeCache::class.java.getDeclaredField("corpCodes")
            field.isAccessible = true
            field.set(cache, corpCodes)
            return OpenDartReader(client, cache)
        }
    }

    // === 공시정보 ===

    suspend fun list(
        corp: String? = null, start: LocalDate? = null, end: LocalDate? = null,
        kind: String = "", kindDetail: String = "", final: Boolean = true,
    ): List<Disclosure> {
        val corpCode = if (corp != null) {
            findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        } else ""
        return DartList.list(client, corpCode, start, end, kind, kindDetail, final)
    }

    suspend fun company(corp: String): Company {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        return DartList.company(client, corpCode)
    }

    suspend fun companyByName(name: String): List<Company> {
        val codes = cache.findByName(name).map { it.corpCode }
        return DartList.companyByName(client, codes)
    }

    suspend fun document(rcpNo: String): String = DartList.document(client, rcpNo)
    suspend fun documentAll(rcpNo: String): List<String> = DartList.documentAll(client, rcpNo)

    fun findCorpCode(corp: String): String? = cache.findCorpCode(corp)

    // === 사업보고서 ===

    suspend fun report(
        corp: String, keyWord: String, bsnsYear: Int, reprtCode: String = "11011",
    ): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        return DartReport.report(client, corpCode, keyWord, bsnsYear, reprtCode)
    }

    // === 재무제표 ===

    suspend fun finstate(corp: String, bsnsYear: Int, reprtCode: String = "11011"): List<FinancialStatement> {
        val corpCode = if ("," in corp) {
            corp.split(",").joinToString(",") { c ->
                findCorpCode(c.trim()) ?: throw IllegalArgumentException("\"${c.trim()}\"을(를) 찾을 수 없습니다")
            }
        } else {
            findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        }
        return DartFinState.finstate(client, corpCode, bsnsYear, reprtCode)
    }

    suspend fun finstateAll(
        corp: String, bsnsYear: Int, reprtCode: String = "11011", fsDiv: String = "CFS",
    ): List<FinancialStatement> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        return DartFinState.finstateAll(client, corpCode, bsnsYear, reprtCode, fsDiv)
    }

    suspend fun finstateXml(rcpNo: String, savePath: Path): Boolean =
        DartFinState.finstateXml(client, rcpNo, savePath)

    suspend fun xbrlTaxonomy(sjDiv: String): List<Map<String, String>> =
        DartFinState.xbrlTaxonomy(client, sjDiv)

    // === 지분공시 ===

    suspend fun majorShareholders(corp: String): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        return DartShare.majorShareholders(client, corpCode)
    }

    suspend fun majorShareholdersExec(corp: String): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        return DartShare.majorShareholdersExec(client, corpCode)
    }

    // === 주요사항보고 ===

    suspend fun event(
        corp: String, keyWord: String, start: LocalDate? = null, end: LocalDate? = null,
    ): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        return DartEvent.event(client, corpCode, keyWord, start, end)
    }

    // === 증권신고서 ===

    suspend fun regstate(
        corp: String, keyWord: String, start: LocalDate? = null, end: LocalDate? = null,
    ): List<Map<String, String>> {
        val corpCode = findCorpCode(corp) ?: throw IllegalArgumentException("\"$corp\"을(를) 찾을 수 없습니다")
        return DartRegState.regstate(client, corpCode, keyWord, start, end)
    }

    // === 웹 스크래핑 ===

    suspend fun listDateEx(date: LocalDate? = null, cache: Boolean = true): List<Disclosure> =
        DartScraper.listDateEx(client, date, cache)

    suspend fun subDocs(rcpNo: String, match: String? = null): List<SubDocument> =
        DartScraper.subDocs(client, rcpNo, match)

    suspend fun attachDocs(rcpNo: String, match: String? = null): List<SubDocument> =
        DartScraper.attachDocs(client, rcpNo, match)

    suspend fun attachFiles(arg: String): Map<String, String> =
        DartScraper.attachFiles(client, arg)

    suspend fun download(url: String, filename: String? = null): Path =
        DartScraper.download(client, url, filename)

    override fun close() {
        client.close()
    }
}
