package io.heapy.kotomi.logging.jul

import io.heapy.kotomi.logging.LoggingContext
import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module
import java.util.logging.Logger

@Module
class KotomiJulHandlerModule {
    context(_: KotomiContext)
    suspend fun loggingContext(): LoggingContext =
        error("loggingContext must be provided")

    context(_: KotomiContext)
    suspend fun julHandler(): KotomiJulHandler =
        KotomiJulHandler(loggingContext()).also { handler ->
            Logger.getLogger("").addHandler(handler)
        }
}
