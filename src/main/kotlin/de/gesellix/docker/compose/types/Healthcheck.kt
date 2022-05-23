package de.gesellix.docker.compose.types

import com.squareup.moshi.Json
import de.gesellix.docker.compose.adapters.CommandType

data class Healthcheck(

        var disable: Boolean? = false,
        var interval: String? = null,
        var retries: Float? = null,
        @CommandType
        var test: Command = Command(),
        var timeout: String? = null,
        @Json(name = "start_period")
        var startPeriod: String? = null
)
