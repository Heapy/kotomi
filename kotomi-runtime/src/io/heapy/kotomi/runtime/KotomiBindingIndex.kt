package io.heapy.kotomi.runtime

/** Versioned binary metadata consumed by the compiler when wiring downstream graphs. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class KotomiBindingIndex(
    val bindings: Array<String> = [],
    val version: Int = 1,
)
