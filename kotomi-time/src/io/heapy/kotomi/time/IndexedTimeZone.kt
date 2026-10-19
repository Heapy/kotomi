package io.heapy.kotomi.time

import io.heapy.kotomi.runtime.IntoSet
import io.heapy.kotomi.runtime.Service

data class IndexedTimeZone(
    val id: String,
)

@Service
fun indexedTimeZone(): IndexedTimeZone =
    IndexedTimeZone("UTC")

interface TimeCodec {
    val name: String
}

private data class NamedTimeCodec(
    override val name: String,
) : TimeCodec

@Service
@IntoSet
fun zoneTimeCodec(): TimeCodec =
    NamedTimeCodec("zone")
