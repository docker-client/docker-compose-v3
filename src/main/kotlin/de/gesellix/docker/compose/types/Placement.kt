package de.gesellix.docker.compose.types

data class Placement(

        var constraints: List<String>? = null,
        var preferences: List<PlacementPreferences>? = null

)
