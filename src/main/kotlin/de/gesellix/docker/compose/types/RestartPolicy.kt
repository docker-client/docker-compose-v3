package de.gesellix.docker.compose.types

import com.squareup.moshi.Json

data class RestartPolicy(

        var condition: String? = null,

        //    Duration Delay
        var delay: String? = null,

        @Json(name = "max_attempts")
        var maxAttempts: Int? = null,

        //    Duration Window
        var window: String? = null
)
