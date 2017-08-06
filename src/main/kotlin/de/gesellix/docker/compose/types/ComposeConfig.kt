package de.gesellix.docker.compose.types

data class ComposeConfig(

        var version: String? = null,
        var services: Map<String, StackService>? = null,
        var networks: Map<String, StackNetwork?>? = null,
        var volumes: Map<String, StackVolume?>? = null,
        var secrets: Map<String, StackSecret>? = null,
        var configs: Map<String, StackConfig>? = null
)
