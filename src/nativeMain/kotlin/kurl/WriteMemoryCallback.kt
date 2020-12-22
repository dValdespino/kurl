package kurl

import kotlinx.cinterop.*
import libcurl.MemoryStruct
import platform.posix.memcpy
import platform.posix.realloc

@ExperimentalUnsignedTypes
internal fun writeMemoryCallback(
    contents: COpaquePointer?,
    size: ULong,
    chunks: ULong,
    userData: COpaquePointer?
): ULong {
    // println("Writing to memory")

    val realSize = size * chunks
    val mem: CPointer<MemoryStruct> = userData!!.reinterpret()
    val pointed = mem.pointed

    pointed.memory = realloc(pointed.memory, pointed.size + realSize + 1u)?.reinterpret()
    if (pointed.memory == null) {
        throw OutOfMemoryError("Ran out of memory while allocating curl response")
    }

    memcpy(pointed.memory + pointed.size.convert(), contents, realSize)
    pointed.size += realSize
    pointed.memory!![pointed.size.convert()] = 0

    return realSize
}
