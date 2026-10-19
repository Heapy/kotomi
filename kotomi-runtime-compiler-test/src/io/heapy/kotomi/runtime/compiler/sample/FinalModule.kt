package io.heapy.kotomi.runtime.compiler.sample

import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module

@Module
class FinalModule {
    var creations: Int = 0
        private set

    context(_: KotomiContext)
    suspend fun leaf(): Any {
        creations++
        return Any()
    }

    context(_: KotomiContext)
    suspend fun root(): List<Any> = listOf(leaf(), leaf())
}
