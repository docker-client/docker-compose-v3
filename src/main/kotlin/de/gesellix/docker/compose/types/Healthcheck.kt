package de.gesellix.docker.compose.types

import de.gesellix.docker.compose.adapters.CommandType

data class Healthcheck(

        val disable: Boolean,
        val interval: String,
        val retries: Float,
        @CommandType
        val test: Command = Command(),
        val timeout: String
)
