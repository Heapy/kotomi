package io.heapy.kotomi.logging.slf4j

import io.heapy.kotomi.logging.LoggingContext
import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.helpers.NOPMDCAdapter
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

/**
 * SLF4J ServiceProvider that bridges SLF4J logging calls to Kotomi Logging.
 *
 * SLF4J instantiates this class through `ServiceLoader`, so the [LoggingContext]
 * cannot be passed in explicitly. Install it through [loggingContext]; every
 * bridged call reads the current value.
 *
 * Register via `META-INF/services/org.slf4j.spi.SLF4JServiceProvider`.
 */
class KotomiSLF4JServiceProvider : SLF4JServiceProvider {

    private val loggerFactory = KotomiLoggerFactory { loggingContext }
    private val markerFactory = BasicMarkerFactory()
    private val mdcAdapter = NOPMDCAdapter()

    override fun getLoggerFactory(): ILoggerFactory = loggerFactory
    override fun getMarkerFactory(): IMarkerFactory = markerFactory
    override fun getMDCAdapter(): MDCAdapter = mdcAdapter
    override fun getRequestedApiVersion(): String = "2.0.99"

    override fun initialize() {}

    companion object {
        @Volatile
        var loggingContext: LoggingContext? = null
    }
}
