package kurl

import kotlinx.cinterop.toKString
import libcurl.CURLE_OK
import libcurl.curl_easy_strerror

public class CurlException(message: String?) : Exception(message)

@ExperimentalUnsignedTypes
public inline fun UInt.checkForCurlError(operation: String): UInt {
    if(this != CURLE_OK) {
        val message = curl_easy_strerror(this)?.toKString()?: "No error message"
        throw CurlException("Operation <$operation> failed: $message")
    }
    return this
}
