package de.gesellix.docker.compose.types

data class Logging(

        val driver: String,
        val options: Map<String, String>
)
