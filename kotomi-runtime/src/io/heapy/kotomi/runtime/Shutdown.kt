package io.heapy.kotomi.runtime

/**
 * Marks a `suspend` function inside a [io.heapy.kotomi.runtime.Module] as a shutdown hook.
 *
 * Shutdown hooks run in strictly reverse [Startup] order, gracefully, awaiting
 * (`join`) each one.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Shutdown
