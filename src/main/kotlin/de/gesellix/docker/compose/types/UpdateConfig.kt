package de.gesellix.docker.compose.types

import com.squareup.moshi.Json

data class UpdateConfig(

        var parallelism: Int? = null,

        //    Duration Delay
        var delay: String? = null,

        @Json(name = "failure_action")
        var failureAction: String? = null,

        //    Duration Monitor
        var monitor: String? = null,

        @Json(name = "max_failure_ratio")
        var maxFailureRatio: Float? = null,

        var order: String? = null
)
