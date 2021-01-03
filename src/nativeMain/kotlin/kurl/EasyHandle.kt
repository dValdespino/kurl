@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package kurl

import kotlinx.cinterop.*
import libcurl.*
import kotlin.native.concurrent.AtomicInt

@ExperimentalUnsignedTypes
/**Creates a new [EasyHandle] and configures it.
 *
 * You may use this method as a builder, or store its result and use it later.*/
public fun easyHandle(setup: EasyHandle.() -> Unit = {}): EasyHandle =
    EasyHandle(curl_easy_init()).init().apply(setup)

@ExperimentalUnsignedTypes
/** Representation of libcurl's easy handle objects.
 *
 * This class can be used with a builder syntax (by chaining calls to configuration methods) or as a regular object.
 * Don't forget to call [cleanup] when you are done using the handle.
 *
 * If you only need to use the handle once, you can call the [use] method, which will automatically dispose it for you.*/
public class EasyHandle internal constructor(public val self: COpaquePointer?) {
    /**A [HandleInfo] instance configured with this [EasyHandle]. Use this to retrieve information related to the last
     * performed call.
     *
     * Note that the [HandleInfo] returned by this property is a persistent object that can be re-used as many times
     * as it is necessary.*/
    public val info: HandleInfo by lazy { HandleInfo(self) }

    /**Atomic flag representing the current state of the handle. Don't modify this yourself unless you absolutely know what you are doing.*/
    @PublishedApi
    internal var state: AtomicInt = AtomicInt(STATE_NEW)

    /**Initialize this handle with some options like a custom write callback (necessary for the [perform]
     *  method implementation]).*/
    internal fun init(): EasyHandle = apply {
        if(!state.compareAndSet(STATE_NEW, STATE_ARMED))
            return@apply

        curl_easy_setopt(self, CURLOPT_WRITEFUNCTION, staticCFunction(::writeMemoryCallback))
        curl_easy_setopt(self, CURLOPT_HEADERFUNCTION, staticCFunction(::writeMemoryCallback))
    }

    /**Run the given [block] on this [EasyHandle], checking whether it has already been disposed,
     *  and throwing an exception in that case.*/
    public inline fun safeRun(catch: Boolean = false, block: EasyHandle.() -> Unit): EasyHandle = try {
        require(state.value != STATE_DISPOSED) { "Attempt to use an EasyHandle after a call to cleanup()" }

        apply(block)
    } catch (e: Throwable) {
        if (catch) this else throw e
    }

    /**Runs [op] on this handle and calls [cleanup] after it is done. Useful for one-time disposable handles.*/
    public inline fun use(op: EasyHandle.() -> Unit) {
        this.op()
        cleanup()
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

    /**Set the next operation to an HTTP GET request.*/
    public inline fun get(): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_HTTPGET, 1L)
    }

    /**Executes an HTTP GET request to the given [targetUrl], or the current url if none is specified,
     *  prior to calling [perform] and returning its result, the [setup] function is applied to the handle.*/
    public inline fun get(targetUrl: String? = null, setup: EasyHandle.() -> Unit = {}): Response? {
        get()
        targetUrl?.let { this.url(it) }
        setup(this)

        return perform()
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
        setup(this)

        return perform()
    }

    /**Setup the [fields] that will be used in subsequent [post] calls.*/
    public inline fun postFields(fields: String) {
        val cString = fields.cstr
        curl_easy_setopt(self, CURLOPT_POSTFIELDS, cString)
        curl_easy_setopt(self, CURLOPT_POSTFIELDSIZE, cString.size)
    }

    /**Output libcurl debugging information to console.*/
    public inline fun verbose(enabled: Boolean = true): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_VERBOSE, if (enabled) 1L else 0L)
    }

    /**Controls whether this handle will enforce the verification of SSL certificates.
     *
     * By default this is set to true.*/
    public inline fun verifyCertificates(enabled: Boolean = true): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_SSL_VERIFYPEER, if (enabled) 1L else 0L)
    }

    /**Make cURL return an actual error if a response's code is 400 or higher, [CurlException] will be thrown in that case.*/
    public inline fun failOnError(enabled: Boolean = true): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_FAILONERROR, if (enabled) 1L else 0L)
    }

    /**Set the referer URL to [value].*/
    public inline fun referer(value: String): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_REFERER, value)
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

    /**Set the [value] of the given [cookie].*/
    public inline fun setCookie(cookie: String, value: String): EasyHandle = safeRun {
        curl_easy_setopt(self, CURLOPT_COOKIE, "$cookie=$value;")
    }

    /**Dispose this handle, it must not be used after this.*/
    public inline fun cleanup() {
        curl_easy_cleanup(self)
        state.value = STATE_DISPOSED
    }

    /**Encode the given [string] to a URL-compliant [String].*/
    public inline fun urlEncode(string: String): String = curl_easy_escape(self, string, string.length)!!.toKString()

    public companion object {
        public const val STATE_NEW: Int = 0
        public const val STATE_ARMED: Int = 1
        public const val STATE_DISPOSED: Int = 2
    }
}
