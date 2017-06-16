package de.gesellix.docker.compose.types

import com.squareup.moshi.Json

data class Limits(

        @Json(name = "cpus")
        var nanoCpus: String? = null,
        //        val MemoryBytes: Long,
        var memory: String? = null
)
