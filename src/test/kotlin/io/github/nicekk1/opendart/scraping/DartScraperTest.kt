package io.github.nicekk1.opendart.scraping

import io.github.nicekk1.opendart.model.SubDocument
import kotlin.test.Test
import kotlin.test.assertEquals

class DartScraperTest {

    @Test
    fun `parseSubDocsMultiPage extracts documents from JavaScript`() {
        val html = """
            <html><head><title>Test</title></head><body><script>
            node1['text'] = "사업보고서";
            node1['id'] = "001";
            node1['rcpNo'] = "20240101000001";
            node1['dcmNo'] = "100";
            node1['eleId'] = "200";
            node1['offset'] = "300";
            node1['length'] = "400";
            node1['dtd'] = "dart3.xsd";
            node1['tocNo'] = "1";
            node2['text'] = "감사보고서";
            node2['id'] = "002";
            node2['rcpNo'] = "20240101000001";
            node2['dcmNo'] = "101";
            node2['eleId'] = "201";
            node2['offset'] = "301";
            node2['length'] = "401";
            node2['dtd'] = "dart3.xsd";
            node2['tocNo'] = "2";
            </script></body></html>
        """.trimIndent()

        val result = DartScraper.parseSubDocsFromHtml(html)
        assertEquals(2, result.size)
        assertEquals("사업보고서", result[0].title)
        assert(result[0].url.contains("rcpNo=20240101000001"))
        assert(result[0].url.contains("dcmNo=100"))
    }

    @Test
    fun `parseAttachDocs extracts options from dropdown`() {
        val html = """
            <html><body>
            <select id="att">
                <option value="null">선택</option>
                <option value="rcpNo=20240101000001&dcmNo=100">사업보고서 본문</option>
                <option value="rcpNo=20240101000001&dcmNo=101">감사보고서</option>
            </select>
            </body></html>
        """.trimIndent()

        val result = DartScraper.parseAttachDocsFromHtml(html)
        assertEquals(2, result.size)
        assertEquals("사업보고서 본문", result[0].title)
        assert(result[0].url.contains("rcpNo=20240101000001"))
    }

    @Test
    fun `parseAttachFiles extracts file links from table`() {
        val html = """
            <html><body>
            <table><tbody>
            <tr><td>report.xls</td><td><a href="/pdf/download/excel.do?rcp_no=123">다운로드</a></td></tr>
            <tr><td>data.pdf</td><td><a href="/pdf/download/pdf.do?rcp_no=123">다운로드</a></td></tr>
            </tbody></table>
            </body></html>
        """.trimIndent()

        val result = DartScraper.parseAttachFilesFromHtml(html)
        assertEquals(2, result.size)
        assertEquals("http://dart.fss.or.kr/pdf/download/excel.do?rcp_no=123", result["report.xls"])
    }

    @Test
    fun `sortByMatch sorts by similarity`() {
        val docs = listOf(
            SubDocument("감사보고서", "url1"),
            SubDocument("사업보고서", "url2"),
            SubDocument("반기보고서", "url3"),
        )
        val sorted = DartScraper.sortByMatch(docs, "사업보고서")
        assertEquals("사업보고서", sorted[0].title)
    }
}
