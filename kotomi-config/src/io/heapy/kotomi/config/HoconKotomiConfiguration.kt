package io.heapy.kotomi.config

import com.typesafe.config.Config
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon.Default.decodeFromConfig

interface KotomiConfiguration {
    fun <T : Any> read(
        deserializer: DeserializationStrategy<T>,
        path: String,
    ): T
}

class MockKotomiConfigurationBuilder {
    private val config = mutableMapOf<DeserializationStrategy<*>, Any>()

    fun <T : Any> add(
        deserializer: DeserializationStrategy<T>,
        value: T,
    ) {
        config[deserializer] = value
    }

    internal fun build(): KotomiConfiguration {
        return MockKotomiConfiguration(config)
    }
}

fun buildMockKotomiConfiguration(
    builder: MockKotomiConfigurationBuilder.() -> Unit,
): KotomiConfiguration {
    val mockKotomiConfigurationBuilder = MockKotomiConfigurationBuilder()
    mockKotomiConfigurationBuilder.builder()
    return mockKotomiConfigurationBuilder.build()
}

internal data class MockKotomiConfiguration(
    private val config: Map<DeserializationStrategy<*>, Any>,
) : KotomiConfiguration {
    override fun <T : Any> read(
        deserializer: DeserializationStrategy<T>,
        path: String,
    ): T {
        @Suppress("UNCHECKED_CAST")
        return config[deserializer] as T
    }
}

internal class HoconKotomiConfiguration(
    private val config: Config,
) : KotomiConfiguration {
    @OptIn(ExperimentalSerializationApi::class)
    override fun <T : Any> read(
        deserializer: DeserializationStrategy<T>,
        path: String
    ): T {
        return decodeFromConfig(
            deserializer = deserializer,
            config = config.getConfig(path),
        )
    }
}
