package de.gesellix.docker.compose.types

import com.squareup.moshi.Json

data class RestartPolicy(

        val condition: String,

        //    Duration Delay
        val delay: String,

        @Json(name = "max_attempts")
        val maxAttempts: Int,

        //    Duration Window
        val window: String
)
