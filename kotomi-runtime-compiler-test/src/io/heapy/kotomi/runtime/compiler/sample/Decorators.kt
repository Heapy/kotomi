package io.heapy.kotomi.runtime.compiler.sample

import io.heapy.kotomi.runtime.*
import io.heapy.kotomi.runtime.wrappers.Retry

interface Flaky {
    @Retry(maxAttempts = 2)
    suspend fun call(): String
    fun default(): String = "interface"
    val calls: Int
}

class FlakyImplementation : Flaky {
    override var calls = 0
    override suspend fun call(): String {
        calls++
        check(calls % 2 == 0) { "retry me" }
        return "ok"
    }
    override fun default(): String = "implementation"
}

@Module class DecoratedModule {
    context(_: KotomiContext)
    suspend fun flaky(): Flaky = FlakyImplementation()
}
