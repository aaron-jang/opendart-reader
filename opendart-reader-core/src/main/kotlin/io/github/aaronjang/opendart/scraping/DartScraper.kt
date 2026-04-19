package io.github.aaronjang.opendart.scraping

import io.github.aaronjang.opendart.cache.DartCache
import io.github.aaronjang.opendart.internal.DartClient
import io.github.aaronjang.opendart.model.Disclosure
import io.github.aaronjang.opendart.model.SubDocument
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DartScraper {

    private const val DART_BASE = "http://dart.fss.or.kr"

    private val MULTI_PAGE_RE = Regex(
        """\s+node[12]\['text'\]\s*=\s*"(.*?)";\s+node[12]\['id'\]\s*=\s*"(\d+)";\s+node[12]\['rcpNo'\]\s*=\s*"(\d+)";\s+node[12]\['dcmNo'\]\s*=\s*"(\d+)";\s+node[12]\['eleId'\]\s*=\s*"(\d+)";\s+node[12]\['offset'\]\s*=\s*"(\d+)";\s+node[12]\['length'\]\s*=\s*"(\d+)";\s+node[12]\['dtd'\]\s*=\s*"(.*?)";\s+node[12]\['tocNo'\]\s*=\s*"(\d+)";"""
    )

    private val SINGLE_PAGE_RE = Regex(
        """\t\tviewDoc\('(\d+)', '(\d+)', '(\d+)', '(\d+)', '(\d+)', '(\S+)',''\);"""
    )

    suspend fun listDateEx(
        client: DartClient, dartCache: DartCache, date: LocalDate? = null, cache: Boolean = true,
    ): List<Disclosure> {
        val d = date ?: LocalDate.now()
        val dateStr = d.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        val result = mutableListOf<Disclosure>()

        for (page in 1..99) {
            delay(100)
            val url = "$DART_BASE/dsac001/search.ax?selectDate=$dateStr&pageGrouping=A&currentPage=$page"
            val html = if (cache) {
                val cacheKey = "html_$url"
                val cached = dartCache.get(cacheKey)
                if (cached != null) {
                    cached
                } else {
                    val text = fetchHtml(client, url)
                    dartCache.put(cacheKey, text)
                    text
                }
            } else {
                fetchHtml(client, url)
            }

            if ("검색된 자료가 없습니다" in html) break

            val doc = Jsoup.parse(html)
            val trs = doc.select("table tbody tr")
            for (tr in trs) {
                val tds = tr.select("td")
                if (tds.size < 6) continue
                val hhmm = tds[0].text().trim()
                val corpClass = tds[1].select("span span").text()
                val name = tds[1].select("span a").text().trim()
                val rcpNo = tds[2].select("a").attr("href").split("=").last()
                val title = tds[2].select("a").text().replace("\\s+".toRegex(), " ").trim()
                val frName = tds[3].text()
                val remark = tds[5].select("span").joinToString("") { it.text() }
                val dt = d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " + hhmm
                result.add(Disclosure(corpCls = corpClass, corpName = name, rceptNo = rcpNo,
                    reportNm = title, rceptDt = dt, flrNm = frName, rm = remark))
            }
        }
        return result
    }

    suspend fun subDocs(client: DartClient, rcpNo: String, match: String? = null): List<SubDocument> {
        val url = if (rcpNo.startsWith("http")) rcpNo else "$DART_BASE/dsaf001/main.do?rcpNo=$rcpNo"
        val html = fetchHtml(client, url)
        val docs = parseSubDocsFromHtml(html)
        return if (match != null) sortByMatch(docs, match) else docs
    }

    suspend fun attachDocs(client: DartClient, rcpNo: String, match: String? = null): List<SubDocument> {
        val url = "$DART_BASE/dsaf001/main.do?rcpNo=$rcpNo"
        val html = fetchHtml(client, url)
        val docs = parseAttachDocsFromHtml(html)
        return if (match != null) sortByMatch(docs, match) else docs
    }

    suspend fun attachFiles(client: DartClient, arg: String): Map<String, String> {
        val url = if (arg.startsWith("http")) arg else "$DART_BASE/dsaf001/main.do?rcpNo=$arg"
        val html = fetchHtml(client, url)
        val rcpDcmRe = Regex("""\s+node[12]\['rcpNo'\]\s*=\s*"(\d+)";\s+node[12]\['dcmNo'\]\s*=\s*"(\d+)";""")
        val m = rcpDcmRe.find(html) ?: return emptyMap()
        val rcpNo = m.groupValues[1]
        val dcmNo = m.groupValues[2]
        val downloadUrl = "$DART_BASE/pdf/download/main.do?rcp_no=$rcpNo&dcm_no=$dcmNo"
        val downloadHtml = fetchHtml(client, downloadUrl)
        return parseAttachFilesFromHtml(downloadHtml)
    }

    suspend fun download(client: DartClient, url: String, filename: String? = null): Path {
        val fn = filename ?: url.split("/").last()
        val response = client.httpClient.get(url) { header("User-Agent", DartClient.USER_AGENT) }
        val bytes = response.readRawBytes()
        val path = Path.of(fn)
        Files.write(path, bytes)
        return path
    }

    fun parseSubDocsFromHtml(html: String): List<SubDocument> {
        val matches = MULTI_PAGE_RE.findAll(html).toList()
        if (matches.isNotEmpty()) {
            return matches.map { m ->
                val (title, _, rcpNo, dcmNo, eleId, offset, length, dtd) = m.destructured
                val params = "rcpNo=$rcpNo&dcmNo=$dcmNo&eleId=$eleId&offset=$offset&length=$length&dtd=$dtd"
                SubDocument(title, "$DART_BASE/report/viewer.do?$params")
            }
        }
        val singleMatches = SINGLE_PAGE_RE.findAll(html).toList()
        if (singleMatches.isNotEmpty()) {
            val doc = Jsoup.parse(html)
            val docTitle = doc.title().trim()
            val m = singleMatches[0]
            val (rcpNo, dcmNo, eleId, offset, length, dtd) = m.destructured
            val params = "rcpNo=$rcpNo&dcmNo=$dcmNo&eleId=$eleId&offset=$offset&length=$length&dtd=$dtd"
            return listOf(SubDocument(docTitle, "$DART_BASE/report/viewer.do?$params"))
        }
        return emptyList()
    }

    fun parseAttachDocsFromHtml(html: String): List<SubDocument> {
        val doc = Jsoup.parse(html)
        val att = doc.selectFirst("#att") ?: return emptyList()
        return att.select("option")
            .filter { it.attr("value") != "null" }
            .map { opt ->
                val title = opt.text().replace("\\s+".toRegex(), " ").trim()
                val url = "$DART_BASE/dsaf001/main.do?${opt.attr("value")}"
                SubDocument(title, url)
            }
    }

    fun parseAttachFilesFromHtml(html: String): Map<String, String> {
        val doc = Jsoup.parse(html)
        val table = doc.selectFirst("table") ?: return emptyMap()
        val result = mutableMapOf<String, String>()
        for (tr in table.select("tbody tr")) {
            val tds = tr.select("td")
            if (tds.size < 2) continue
            val fname = tds[0].text()
            val href = tds[1].select("a").attr("href")
            if (href.isNotEmpty()) result[fname] = "$DART_BASE$href"
        }
        return result
    }

    fun sortByMatch(docs: List<SubDocument>, match: String): List<SubDocument> {
        return docs.sortedByDescending { similarity(it.title, match) }
    }

    private fun similarity(a: String, b: String): Double {
        if (a.isEmpty() && b.isEmpty()) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val matches = lcs(a, b)
        return 2.0 * matches / (a.length + b.length)
    }

    private fun lcs(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1] + 1
                           else maxOf(dp[i - 1][j], dp[i][j - 1])
            }
        }
        return dp[a.length][b.length]
    }

    private suspend fun fetchHtml(client: DartClient, url: String): String {
        val response = client.httpClient.get(url) { header("User-Agent", DartClient.USER_AGENT) }
        return response.bodyAsText()
    }
}
