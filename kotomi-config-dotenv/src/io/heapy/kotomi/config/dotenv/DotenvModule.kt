package io.heapy.kotomi.config.dotenv

import io.heapy.kotomi.config.common.Environment
import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module

@Module
class DotenvModule {
    context(_: KotomiContext)
    suspend fun dotenv(): Environment =
        io.heapy.kotomi.config.dotenv.dotenv()
}
