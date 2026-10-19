package io.heapy.kotomi.config.common

object EmptyEnvironment : Environment {
    override val properties: Map<String, String> = emptyMap()
}
