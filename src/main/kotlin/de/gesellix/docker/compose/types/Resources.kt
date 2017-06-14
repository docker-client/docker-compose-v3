package de.gesellix.docker.compose.types

data class Resources(

        val limits: Limits,
        val reservations: Reservations
)
