package io.heapy.kotomi.runtime.wrappers

/**
 * Re-invokes the annotated `suspend` method up to [maxAttempts] times on failure, with an
 * optional [delayMillis] between attempts. Cancellation is never retried.
 *
 * Innermost of the production wrapper chain: `@Log` -> `@Metric` -> `@Retry` -> delegate,
 * so only the delegate call is retried — one public call is logged once.
 */
@io.heapy.kotomi.runtime.CallWrapper("io.heapy.kotomi.runtime.wrappers.retryCall", 300)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Retry(
    val maxAttempts: Int = 3,
    val delayMillis: Long = 0,
)
