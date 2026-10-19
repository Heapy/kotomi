package io.heapy.kotomi.time

import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module
import java.time.Clock

@Module
interface TimeSourceModule {
    context(_: KotomiContext)
    suspend fun timeSource(): TimeSource =
        TimeSource(
            clock = Clock.systemDefaultZone(),
        )
}
