package io.github.aaronjang.opendart.internal

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateUtilsTest {
    @Test
    fun `parseDate with LocalDate returns same date`() {
        val date = LocalDate.of(2024, 1, 15)
        assertEquals(date, parseDate(date))
    }

    @Test
    fun `parseDate with null returns null`() {
        assertEquals(null, parseDate(null))
    }

    @Test
    fun `formatDate formats as yyyyMMdd`() {
        val date = LocalDate.of(2024, 3, 5)
        assertEquals("20240305", formatDate(date))
    }

    @Test
    fun `defaultStart returns 1900-01-01`() {
        assertEquals(LocalDate.of(1900, 1, 1), defaultStart())
    }

    @Test
    fun `defaultEnd returns today`() {
        assertEquals(LocalDate.now(), defaultEnd())
    }
}
