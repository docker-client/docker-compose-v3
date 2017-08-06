package de.gesellix.docker.compose.types

import com.squareup.moshi.Json
import de.gesellix.docker.compose.adapters.DriverOptsType
import de.gesellix.docker.compose.adapters.ExternalType
import de.gesellix.docker.compose.adapters.LabelsType

data class StackVolume(

        var name: String? = "",

        var driver: String? = null,

        @Json(name = "driver_opts")
        @DriverOptsType
        var driverOpts: DriverOpts = DriverOpts(),

        // StackVolume.external.name is deprecated and replaced by StackVolume.name
        @ExternalType
        var external: External = External(),

        @LabelsType
        var labels: Labels = Labels()
)
