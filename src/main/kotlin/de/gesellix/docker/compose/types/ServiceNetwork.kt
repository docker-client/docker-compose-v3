package de.gesellix.docker.compose.types

import com.squareup.moshi.Json

data class ServiceNetwork(

        var aliases: List<String> = listOf(),

        @Json(name = "ipv4_address")
        var ipv4Address: String = "",

        @Json(name = "ipv6_address")
        var ipv6Address: String = ""
)
