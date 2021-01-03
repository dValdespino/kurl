package kurl

public data class Response internal constructor(
    val code: Int,
    val header: String,
    val body: String
)
