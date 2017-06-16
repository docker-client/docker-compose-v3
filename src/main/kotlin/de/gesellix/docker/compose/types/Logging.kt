package de.gesellix.docker.compose.types

data class Logging(

        var driver: String? = null,
        var options: Map<String, String>? = null
)
