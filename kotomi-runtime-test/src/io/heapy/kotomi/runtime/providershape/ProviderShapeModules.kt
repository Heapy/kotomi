package io.heapy.kotomi.runtime.providershape

import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module

class ShapeService(val value: String)

@Module
class ProviderShapeModule {
    context(_: KotomiContext)
    suspend fun service(): ShapeService =
        ShapeService(
            value = listOf(
                privateHelper(),
                publicHelperWithValueParam("param"),
                nonSuspendHelper(),
            ).joinToString(":"),
        )

    context(_: KotomiContext)
    private suspend fun privateHelper(): String =
        "private"

    context(_: KotomiContext)
    suspend fun publicHelperWithValueParam(value: String): String =
        value

    context(_: KotomiContext)
    fun nonSuspendHelper(): String =
        "non-suspend"

    context(_: KotomiContext)
    suspend fun unitHelper() {
        // Unit-returning helpers are not provider bindings.
    }
}
