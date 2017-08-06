package de.gesellix.docker.compose.types

data class ServiceVolume(

        var type: String = "",
        var source: String = "",
        var target: String = "",
        var readOnly: Boolean = false,
        var consistency: String = "",
        var bind: ServiceVolumeBind? = null,
        var volume: ServiceVolumeVolume? = null
)
