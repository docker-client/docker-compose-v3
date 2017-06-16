package de.gesellix.docker.compose.types

data class Resources(

        var limits: Limits? = null,
        var reservations: Reservations? = null
)
