@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package kurl

import kotlinx.cinterop.*
import libcurl.*

@ExperimentalUnsignedTypes
/** Representation of libcurl's easy handle objects.
 *
 * This class can be used with a builder syntax (by chaining calls to configuration methods) or as a regular object.
 * Don't forget to call [cleanup] when you are done using the handle.
 *
 * If you only need to use the handle once, you can call the [use] method, which will automatically dispose it for you.*/
public class EasyHandle internal constructor(public val self: COpaquePointer?) {

    public val info: HandleInfo by lazy { HandleInfo(self) }
    public var disposed: Boolean = false

    /**Initialize this handle with some options specific to our kotlin implementation, like the write callback.*/
    internal fun init(): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_WRITEFUNCTION, staticCFunction(::writeMemoryCallback))
        curl_easy_setopt(self, CURLOPT_HEADERFUNCTION, staticCFunction(::writeMemoryCallback))
    }

    public inline fun safeRun(block: EasyHandle.() -> Unit): EasyHandle {
        require(!disposed) { "Attempt to use an EasyHandle after a call to cleanup()" }

        return apply(block)
    }

    /**Controls whether this handle will enforce the verification of SSL certificates.
     *
     * By default this is set to true.*/
    public inline fun verifyCertificates(enabled: Boolean = true): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_SSL_VERIFYPEER, if (enabled) 1L else 0L)
    }

    public inline fun failOnError(enabled: Boolean = true): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_FAILONERROR, if (enabled) 1L else 0L)
    }

    /**Output libcurl debugging information.*/
    public inline fun verbose(enabled: Boolean = true): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_VERBOSE, if (enabled) 1L else 0L)
    }

    public inline fun referer(value: String): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_REFERER, value)
    }

    /**Set the target [url].
     *
     * This setting will persist until the next call to this method, note however, that some helper methods like
     * [get] might change the url when called.*/
    public inline fun url(url: String): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_URL, url)
    }

    /**Perform the configured operation (by default an HTTP GET operation) and return the response as a string.*/
    public fun perform(): Response? = memScoped {
        // MemoryStruct holding the buffer for the response header
        val headerBuffer = alloc<MemoryStruct>()

        // MemoryStruct holding the buffer for the response body
        val bodyBuffer = alloc<MemoryStruct>()

        // The WRITEDATA and HEADERDATA options point to the buffer used by the WriteMemoryCallback to write the response
        curl_easy_setopt(self, CURLOPT_WRITEDATA, bodyBuffer.ptr)
        curl_easy_setopt(self, CURLOPT_HEADERDATA, headerBuffer.ptr)

        // Perform and check for errors
        curl_easy_perform(self)
            .checkForCurlError("Perform")

        // Retrieve the data written in the buffers
        val header = headerBuffer.memory?.toKString() ?: return null
        val body = bodyBuffer.memory?.toKString() ?: ""

        return@memScoped Response(info.responseCode.toInt(), header, body)
    }

    /**Set the next operation to HTTP POST.*/
    public inline fun post(): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_HTTPGET, 1L)
    }

    /**Sets up this handle for a POST operation, optionally setting the [targetUrl] and the form [fields] beforehand.
     * The [setup] block is applied before running [perform] to obtain the result*/
    public inline fun post(
        targetUrl: String? = null,
        fields: String? = null,
        setup: EasyHandle.() -> Unit = {}
    ): Response? {
        post()
        targetUrl?.let { url(it) }
        fields?.let { postFields(it) }
        setup()

        return perform()
    }

    public inline fun postFields(fields: String) {
        val cString = fields.cstr
        curl_easy_setopt(self, CURLOPT_POSTFIELDS, cString)
        curl_easy_setopt(self, CURLOPT_POSTFIELDSIZE, cString.size)
    }

    /**Whether to include response headers in the body of [perform] responses.
     *
     * This option should be use for testing only, since it can lead to very confusing [Response] outputs.*/
    public inline fun includeHeaders(enabled: Boolean = true): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_HEADER, if (enabled) 1L else 0L)
    }

    /**Change the user agent name displayed by this handle.*/
    public inline fun userAgent(agent: String): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_USERAGENT, agent)
    }

    /**Set the next operation to an HTTP GET request.*/
    public inline fun get(): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_HTTPGET, 1L)
    }

    /**Executes an HTTP GET request to the given [targetUrl], or the current url if none is specified,
     *  prior to calling [perform] and returning its result, the [setup] function is applied to the handle.*/
    public inline fun get(targetUrl: String? = null, setup: EasyHandle.() -> Unit = {}): Response? {
        get()
        targetUrl?.let { this.url(it) }
        setup()

        return perform()
    }

    /**Set the [value] of the given [cookie].*/
    public inline fun setCookie(cookie: String, value: String): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_COOKIE, "$cookie=$value;")
    }

    /**Dispose this handle, it must not be used after this.*/
    public inline fun cleanup() {
        curl_easy_cleanup(self)
        disposed = true
    }

    /**Runs [op] on this handle and calls [cleanup] after it is done. Useful for one-time disposable handles.*/
    public inline fun use(op: EasyHandle.() -> Unit) {
        op()
        cleanup()
    }

    /**Encode the given [string] to a URL-compliant [String].*/
    public inline fun urlEncode(string: String): String = curl_easy_escape(self, string, string.length)!!.toKString()
}

@ExperimentalUnsignedTypes
/**Creates a new [EasyHandle] and configures it.
 *
 * You may use this method as a builder, or store its result and use it later.*/
public fun easyHandle(setup: EasyHandle.() -> Unit = {}): EasyHandle =
    EasyHandle(curl_easy_init()).init().apply(setup)
