package io.heapy.kotomi.runtime.allopen

import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module

/** Final classes and methods retain their JVM shape while IR instruments provider bodies. */

class Setting(val value: String)

class Service(val setting: String)

@Module
class NoOpenModule {
    context(_: KotomiContext)
    suspend fun setting(): Setting =
        Setting("configured")

    context(_: KotomiContext)
    suspend fun service(): Service =
        Service(setting().value)
}
