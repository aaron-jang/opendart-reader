package io.github.aaronjang.opendart.exception

class DartException(
    val status: String,
    override val message: String
) : RuntimeException("[$status] $message")
