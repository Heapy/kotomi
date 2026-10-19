package io.heapy.kotomi.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.heapy.kotomi.config.dotenv.DotenvModule
import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module

@Module
class ConfigurationModule(
    private val dotenvModule: DotenvModule,
) {
    context(_: KotomiContext)
    suspend fun config(): KotomiConfiguration =
        HoconKotomiConfiguration(hoconConfig())

    context(_: KotomiContext)
    suspend fun environmentOverrides(): Map<String, String> =
        mapOf()

    context(_: KotomiContext)
    suspend fun systemEnvironment(): Map<String, String> =
        System.getenv()

    context(_: KotomiContext)
    suspend fun effectiveEnvironment(): Map<String, String> =
        buildMap {
            putAll(systemEnvironment())
            putAll(dotenvModule.dotenv().properties)
            putAll(environmentOverrides())
        }

    context(_: KotomiContext)
    suspend fun hoconConfig(): Config {
        // Set system properties, so Hocon can read them and use as substitution variables
        effectiveEnvironment().forEach {
            System.setProperty(
                it.key,
                it.value
            )
        }

        return ConfigFactory.load()
    }
}
