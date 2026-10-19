package io.heapy.kotomi.time

data class ProvidersFromTimeZone(
    val id: String,
)

object LegacyTimeProviders {
    fun providersFromTimeZone(): ProvidersFromTimeZone =
        ProvidersFromTimeZone("providers-from")
}
