package de.gesellix.docker.compose.types

data class Ipam(

        var driver: String? = null,
        var config: List<Config>? = null
)
