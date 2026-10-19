package io.heapy.kotomi.logging.slf4j

import io.heapy.kotomi.logging.LoggingContext
import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap

/**
 * Logger factory that creates [KotomiSLF4JLogger] instances.
 */
class KotomiLoggerFactory(
    private val context: () -> LoggingContext?,
) : ILoggerFactory {

    private val loggers = ConcurrentHashMap<String, Logger>()

    override fun getLogger(name: String): Logger =
        loggers.computeIfAbsent(name) { KotomiSLF4JLogger(it, context) }
}
