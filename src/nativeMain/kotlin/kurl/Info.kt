@file:Suppress("unused", "SpellCheckingInspection")

package kurl

import kotlinx.cinterop.*
import kotlinx.cinterop.nativeHeap.alloc
import libcurl.*

public class HandleInfo (public val handle: COpaquePointer?) {
    public inline val effectiveUrl: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_EFFECTIVE_URL , data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_EFFECTIVE_URL")
        }

    public inline val responseCode: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, data.ptr)

        return data.value
    }

    public inline val totalTime: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_TOTAL_TIME, data.ptr)

        return data.value
    }

    public inline val nameLookupTime: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_NAMELOOKUP_TIME, data.ptr)

        return data.value
    }

    public inline val connectTime: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_CONNECT_TIME, data.ptr)

        return data.value
    }

    public inline val preTransferTime: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_PRETRANSFER_TIME, data.ptr)

        return data.value
    }

    public inline val sizeUpload: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_SIZE_UPLOAD, data.ptr)

        return data.value
    }

    public inline val sizeDownload: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_SIZE_DOWNLOAD, data.ptr)

        return data.value
    }

    public inline val speedDownload: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_SPEED_DOWNLOAD, data.ptr)

        return data.value
    }

    public inline val speedUpload: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_SPEED_UPLOAD, data.ptr)

        return data.value
    }

    public inline val headerSize: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_HEADER_SIZE, data.ptr)

        return data.value
    }

    public inline val requestSize: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_REQUEST_SIZE, data.ptr)

        return data.value
    }

    public inline val sslVerifyResult: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_SSL_VERIFYRESULT, data.ptr)

        return data.value
    }

    public inline val fileTime: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_FILETIME, data.ptr)

        return data.value
    }

    public inline val contentLengthDownload: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_CONTENT_LENGTH_DOWNLOAD, data.ptr)

        return data.value
    }

    public inline val contentLengthUpload: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_CONTENT_LENGTH_UPLOAD, data.ptr)

        return data.value
    }

    public inline val startTransferTime: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_STARTTRANSFER_TIME, data.ptr)

        return data.value
    }

    public inline val contentType: String
    get() = memScoped {
        val data = allocPointerTo<ByteVar>()
        curl_easy_getinfo(handle, CURLINFO_CONTENT_TYPE, data.ptr)

        return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_CONTENT_TYPE")
    }

    public inline val redirectTime: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_REDIRECT_TIME, data.ptr)

        return data.value
    }

    public inline val redirectCount: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_REDIRECT_COUNT, data.ptr)

        return data.value
    }

    public inline val private: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_PRIVATE, data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_PRIVATE")
        }

    public inline val httpConnectCode: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_HTTP_CONNECTCODE, data.ptr)

        return data.value
    }

    public inline val httpAuthAvail: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_HTTPAUTH_AVAIL, data.ptr)

        return data.value
    }

    public inline val proxyAuthAvail: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_PROXYAUTH_AVAIL, data.ptr)

        return data.value
    }

    public inline val osErrno: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_OS_ERRNO, data.ptr)

        return data.value
    }

    public inline val numConnects: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_NUM_CONNECTS, data.ptr)

        return data.value
    }

    public inline val lastSocket: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_LASTSOCKET, data.ptr)

        return data.value
    }

    public inline val ftpEntryPath: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_FTP_ENTRY_PATH, data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_FTP_ENTRY_PATH")
        }

    public inline val redirectUrl: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_REDIRECT_URL, data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_REDIRECT_URL")
        }

    public inline val primaryIp: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_PRIMARY_IP, data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_PRIMARY_IP")
        }

    public inline val appConnectTime: Double get() = memScoped {
        val data = alloc<DoubleVar>()
        curl_easy_getinfo(handle, CURLINFO_APPCONNECT_TIME, data.ptr)

        return data.value
    }

    public inline val certInfo: COpaquePointer? get() = memScoped {
        val data = alloc<COpaquePointerVar>()
        curl_easy_getinfo(handle, CURLINFO_CERTINFO, data.ptr)

        return data.value
    }

    public inline val conditionUnmet: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_CONDITION_UNMET, data.ptr)

        return data.value
    }

    public inline val rtspSessionId: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_RTSP_SESSION_ID, data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_RTSP_SESSION_ID")
        }

    public inline val rtspClientCseq: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_RTSP_CLIENT_CSEQ, data.ptr)

        return data.value
    }

    public inline val rtspServerCseq: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_RTSP_SERVER_CSEQ, data.ptr)

        return data.value
    }

    public inline val rtspCseqRecv: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_RTSP_CSEQ_RECV, data.ptr)

        return data.value
    }

    public inline val primaryPort: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_PRIMARY_PORT, data.ptr)

        return data.value
    }

    public inline val localIp: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_LOCAL_IP, data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_LOCAL_IP")
        }

    public inline val localPort: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_LOCAL_PORT, data.ptr)

        return data.value
    }

    public inline val tlsSession: COpaquePointer? get() = memScoped {
        val data = alloc<COpaquePointerVar>()
        curl_easy_getinfo(handle, CURLINFO_TLS_SESSION, data.ptr)

        return data.value
    }

    public inline val tlsSSLPtr: COpaquePointer? get() = memScoped {
        val data = alloc<COpaquePointerVar>()
        curl_easy_getinfo(handle, CURLINFO_TLS_SSL_PTR, data.ptr)

        return data.value
    }

    public inline val httpVersion: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_HTTP_VERSION, data.ptr)

        return data.value
    }

    public inline val proxySslVerifyResult: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_PROXY_SSL_VERIFYRESULT, data.ptr)

        return data.value
    }

    public inline val protocol: Long get() = memScoped {
        val data = alloc<LongVar>()
        curl_easy_getinfo(handle, CURLINFO_PROTOCOL, data.ptr)

        return data.value
    }

    public inline val scheme: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_SCHEME, data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_SCHEME")
        }

    public inline val effectiveMethod: String
        get() = memScoped {
            val data = allocPointerTo<ByteVar>()
            curl_easy_getinfo(handle, CURLINFO_EFFECTIVE_METHOD, data.ptr)

            return data.value?.toKString() ?: throw CurlException("Error retrieving requested info: CURLINFO_EFFECTIVE_METHOD")
        }

}
