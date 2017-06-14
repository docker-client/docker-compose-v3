package de.gesellix.docker.compose.types

data class PortConfig(

        var mode: String?,
        var target: Int?,
        var published: Int?,
        var protocol: String?
)
