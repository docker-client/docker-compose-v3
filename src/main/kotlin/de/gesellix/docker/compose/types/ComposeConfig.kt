package de.gesellix.docker.compose.types

data class ComposeConfig(

        var version: String? = null,
        var services: Map<String, Service>? = null,
        var networks: Map<String, Network?>? = null,
        var volumes: Map<String, Volume?>? = null,
        var secrets: Map<String, Secret>? = null
)
