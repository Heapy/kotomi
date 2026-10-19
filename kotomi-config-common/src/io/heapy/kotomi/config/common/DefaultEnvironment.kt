package io.heapy.kotomi.config.common

data class DefaultEnvironment(
    override val properties: Map<String, String>,
) : Environment
