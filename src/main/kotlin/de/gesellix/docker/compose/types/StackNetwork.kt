package de.gesellix.docker.compose.types

import com.squareup.moshi.Json
import de.gesellix.docker.compose.adapters.DriverOptsType
import de.gesellix.docker.compose.adapters.ExternalType
import de.gesellix.docker.compose.adapters.LabelsType

data class StackNetwork(

        var driver: String? = null,
        @Json(name = "driver_opts")
        @DriverOptsType
        var driverOpts: DriverOpts = DriverOpts(),
        var ipam: Ipam? = null,
        @ExternalType
        var external: External = External(),
        var internal: Boolean? = null,
        var attachable: Boolean? = false,
        @LabelsType
        var labels: Labels? = null,
        var name: String? = null
)
