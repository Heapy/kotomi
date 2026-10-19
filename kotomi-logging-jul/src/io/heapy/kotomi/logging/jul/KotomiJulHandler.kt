package io.heapy.kotomi.logging.jul

import io.heapy.kotomi.logging.Level
import io.heapy.kotomi.logging.LogEvent
import io.heapy.kotomi.logging.LoggingContext
import io.heapy.kotomi.logging.Mdc
import io.heapy.kotomi.logging.ThrowableProxy
import kotlin.time.Instant
import java.util.logging.Handler
import java.util.logging.LogRecord

/**
 * java.util.logging Handler that bridges JUL logging calls to Kotomi Logging.
 *
 * **No global state.** Receives a fixed [LoggingContext] at construction time.
 *
 * Usage:
 * ```
 * val julHandler = KotomiJulHandler(loggingContext)
 * java.util.logging.Logger.getLogger("").addHandler(julHandler)
 * ```
 */
class KotomiJulHandler(
    private val context: LoggingContext,
) : Handler() {
    override fun publish(record: LogRecord?) {
        record ?: return

        val level = mapLevel(record.level)
        val loggerName = record.loggerName ?: "unknown"
        if (!context.handler.isEnabled(level, loggerName)) return

        val event = LogEvent(
            level = level,
            loggerName = loggerName,
            message = record.message ?: "",
            throwable = record.thrown?.let { ThrowableProxy.create(it) },
            timestamp = Instant.fromEpochMilliseconds(record.millis),
            mdc = Mdc.Empty,
        )

        context.handler.handle(event)
    }

    override fun flush() {}

    override fun close() {}

    private fun mapLevel(level: java.util.logging.Level): Level = when {
        level.intValue() >= java.util.logging.Level.SEVERE.intValue() -> Level.ERROR
        level.intValue() >= java.util.logging.Level.WARNING.intValue() -> Level.WARN
        level.intValue() >= java.util.logging.Level.INFO.intValue() -> Level.INFO
        level.intValue() >= java.util.logging.Level.FINE.intValue() -> Level.DEBUG
        else -> Level.TRACE
    }
}
