package io.heapy.kotomi.runtime.interfaces

import io.heapy.kotomi.runtime.IntoSet
import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module

/**
 * Interface-module sample: no-arg default methods are leaf providers and methods with
 * value parameters are param-wired by exact return type.
 */
@Module
interface InterfaceLeafModule {
    context(_: KotomiContext)
    suspend fun answer(): Int = 42
}

data class InterfaceHost(val value: String)
data class InterfacePort(val value: Int)
data class InterfaceEndpoint(val value: String)

@Module
interface InterfaceParamModule {
    context(_: KotomiContext)
    suspend fun host(): InterfaceHost =
        InterfaceHost("localhost")

    context(_: KotomiContext)
    suspend fun port(): InterfacePort =
        InterfacePort(8080)

    context(_: KotomiContext)
    suspend fun endpoint(
        host: InterfaceHost,
        port: InterfacePort,
    ): InterfaceEndpoint =
        InterfaceEndpoint("${host.value}:${port.value}")
}

interface InterfaceCodec {
    val name: String
}

data class InterfaceNamedCodec(
    override val name: String,
) : InterfaceCodec

data class InterfaceCodecRegistry(
    val codecs: Set<InterfaceCodec>,
) {
    fun names(): List<String> =
        codecs.map { it.name }
}

@Module
interface InterfaceSetModule {
    @IntoSet
    context(_: KotomiContext)
    suspend fun jsonInterfaceCodec(): InterfaceCodec =
        InterfaceNamedCodec("json")

    @IntoSet
    context(_: KotomiContext)
    suspend fun xmlInterfaceCodec(): InterfaceCodec =
        InterfaceNamedCodec("xml")

    context(_: KotomiContext)
    suspend fun interfaceCodecRegistry(
        codecs: Set<InterfaceCodec>,
    ): InterfaceCodecRegistry =
        InterfaceCodecRegistry(codecs)
}
