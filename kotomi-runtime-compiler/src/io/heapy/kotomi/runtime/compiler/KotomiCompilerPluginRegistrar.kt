package io.heapy.kotomi.runtime.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class KotomiCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = "io.heapy.kotomi.runtime"
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(KotomiFirExtensionRegistrar())
        IrGenerationExtension.registerExtension(KotomiIrGenerationExtension(configuration))
    }
}
