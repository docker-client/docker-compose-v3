package de.gesellix.docker.compose.types

import com.squareup.moshi.Json

data class UpdateConfig(

        val parallelism: Int,

        //    Duration Delay
        val delay: String,

        @Json(name = "failure_action")
        val failureAction: String,

        //    Duration Monitor
        val monitor: String,

        @Json(name = "max_failure_ratio")
        val maxFailureRatio: Float
)
