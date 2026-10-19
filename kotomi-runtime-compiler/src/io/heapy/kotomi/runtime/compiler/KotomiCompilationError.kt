package io.heapy.kotomi.runtime.compiler

/** Expected source error, reported through Kotlin's diagnostic API instead of crashing IR. */
internal class KotomiCompilationError(message: String) : RuntimeException(message)

internal inline fun kotomiRequire(value: Boolean, message: () -> String) {
    if (!value) throw KotomiCompilationError(message())
}

internal fun kotomiError(message: String): Nothing = throw KotomiCompilationError(message)
