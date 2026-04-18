package io.github.nicekk1.opendart.exception

class DartException(
    val status: String,
    override val message: String
) : RuntimeException("[$status] $message")
