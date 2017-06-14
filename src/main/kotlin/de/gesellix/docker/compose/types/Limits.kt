package de.gesellix.docker.compose.types

import com.squareup.moshi.Json

data class Limits(

        @Json(name = "cpus")
        val nanoCpus: String,
//        val MemoryBytes: Long,
        val memory: String
)
