package de.gesellix.docker.compose.types

data class ServiceVolume(

        var type: String = "",
        var source: String = "",
        var target: String = "",
        var readOnly: Boolean, // `mapstructure:"read_only"`
        var consistency: String = "",
        var bind: ServiceVolumeBind,
        var volume: ServiceVolumeVolume

)
