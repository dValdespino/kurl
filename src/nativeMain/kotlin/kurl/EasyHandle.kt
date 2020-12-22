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

    /**Initialize this handle with some options specific to our kotlin implementation, like the write callback.*/
    internal fun init(): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_WRITEFUNCTION, staticCFunction(::writeMemoryCallback))
    }

    /**Controls whether this handle will enforce the verification of SSL certificates.
     *
     * By default this is set to true.*/
    public inline fun verifyCertificates(enabled: Boolean = true): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_SSL_VERIFYPEER, if (enabled) 1L else 0L);
    }

    /**Output libcurl debugging information.*/
    public inline fun verbose(enabled: Boolean = true): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_VERBOSE, if (enabled) 1L else 0L)
    }

    /**Set the target [url].
     *
     * This setting will persist until the next call to this method, note however, that some helper methods like
     * [get] might change the url when called.*/
    public inline fun url(url: String): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_URL, url)
    }

    /**Perform the configured operation (by default an HTTP GET operation) and return the response as a string.*/
    public fun perform(): String = memScoped {
        val buffer = alloc<MemoryStruct>()

        curl_easy_setopt(self, CURLOPT_WRITEDATA, buffer.ptr)

        val curlCode = curl_easy_perform(self)

        if (curlCode != CURLE_OK)
            throw Exception("cURL returned error on response: ${curl_easy_strerror(curlCode)?.toKString()}")

        buffer.memory?.toKString()
            ?: throw Exception("Unknown error decoding curl response (buffer memory pointer is null)")
    }

    // TODO: 12/21/2020 Fix the bug that sends incorrect data when posting from Kotlin bindings
    /**Executes an HTTP POST operation, setting [fields] as the form fields in the request's body. Returns the response.*/
    public inline fun post(fields: String): String {
        // curl_easy_setopt(self, CURLOPT_POST, 1L)
        return memScoped {

            val buffer = alloc<MemoryStruct>()

            curl_easy_setopt(self, CURLOPT_WRITEDATA, buffer.ptr)

            val curlCode = curl_post(self, fields.cstr)

            if (curlCode != CURLE_OK)
                throw Exception("cURL returned error on response: ${curl_easy_strerror(curlCode)?.toKString()}")

            buffer.memory?.toKString()
                ?: throw Exception("Unknown error decoding curl response (buffer memory pointer is null)")
        }
    }

    /* inline fun post(setup: EasyHandle.() -> Unit): String {
        post()
        setup()

        return memScoped {

            val buffer = alloc<MemoryStruct>()

            curl_easy_setopt(self, CURLOPT_WRITEDATA, buffer.ptr)

            val curlCode = curl_post(self)

            if (curlCode != CURLE_OK)
                throw Exception("cURL returned error on response: ${curl_easy_strerror(curlCode)?.toKString()}")

            buffer.memory?.toKString()
                ?: throw Exception("Unknown error decoding curl response (buffer memory pointer is null)")
        }
    }*/

    /* inline fun postFields(fields: String) {
        curl_easy_setopt(self, CURLOPT_POSTFIELDS, fields.cstr)
    }*/

    /**Whether to include response headers in the result of [perform].*/
    public inline fun includeHeaders(enabled: Boolean = true): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_HEADER, if (enabled) 1L else 0L)
    }

    /**Change the user agent name displayed by this handle.*/
    public inline fun userAgent(agent: String): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_USERAGENT, agent)
    }

    /**Set the next operation to an HTTP GET request.*/
    public inline fun get(): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_HTTPGET, 1L)
    }

    /**Executes an HTTP GET request to the given [targetUrl], or the current url if none is specified,
     *  prior to calling [perform] and returning its result, the [setup] function is applied to the handle.*/
    public inline fun get(targetUrl: String? = null, setup: EasyHandle.() -> Unit = {}): String {
        get()
        targetUrl?.let { this.url(it) }
        setup()

        return perform()
    }

    /**Set the [value] of the given [cookie].*/
    public inline fun setCookie(cookie: String, value: String): EasyHandle = apply {
        curl_easy_setopt(self, CURLOPT_COOKIE, "$cookie=$value;")
    }

    /**Dispose this handle, it must not be used after this.*/
    public inline fun cleanup() {
        curl_easy_cleanup(self)
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
