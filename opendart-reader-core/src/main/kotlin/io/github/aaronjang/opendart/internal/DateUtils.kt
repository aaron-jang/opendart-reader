package io.github.aaronjang.opendart.internal

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val FORMAT_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd")

fun parseDate(date: Any?): LocalDate? = when (date) {
    null -> null
    is LocalDate -> date
    else -> throw IllegalArgumentException("Unsupported date type: ${date::class}")
}

fun formatDate(date: LocalDate): String = date.format(FORMAT_YYYYMMDD)
fun defaultStart(): LocalDate = LocalDate.of(1900, 1, 1)
fun defaultEnd(): LocalDate = LocalDate.now()
