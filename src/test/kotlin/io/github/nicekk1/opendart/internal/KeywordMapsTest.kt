package io.github.nicekk1.opendart.internal

import kotlin.test.Test
import kotlin.test.assertEquals

class KeywordMapsTest {
    @Test
    fun `report keyword map has 28 entries`() {
        assertEquals(28, REPORT_KEYWORD_MAP.size)
    }

    @Test
    fun `report keyword map contains expected entries`() {
        assertEquals("alotMatter", REPORT_KEYWORD_MAP["배당"])
        assertEquals("empSttus", REPORT_KEYWORD_MAP["직원"])
        assertEquals("tesstkAcqsDspsSttus", REPORT_KEYWORD_MAP["자기주식"])
    }

    @Test
    fun `event keyword map has 36 entries`() {
        assertEquals(36, EVENT_KEYWORD_MAP.size)
    }

    @Test
    fun `event keyword map contains expected entries`() {
        assertEquals("piicDecsn", EVENT_KEYWORD_MAP["유상증자"])
        assertEquals("cmpMgDecsn", EVENT_KEYWORD_MAP["회사합병"])
    }

    @Test
    fun `regstate keyword map has 6 entries`() {
        assertEquals(6, REGSTATE_KEYWORD_MAP.size)
    }

    @Test
    fun `regstate keyword map contains expected entries`() {
        assertEquals("mgRs", REGSTATE_KEYWORD_MAP["합병"])
        assertEquals("bdRs", REGSTATE_KEYWORD_MAP["채무증권"])
    }
}
