package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.DriverOpts

class MapToDriverOptsAdapter {

    @ToJson
    fun toJson(@DriverOptsType driverOpts: DriverOpts): Map<String, String> {
        throw  UnsupportedOperationException()
    }

    @FromJson
    @DriverOptsType
    fun fromJson(options: Map<String, String>): DriverOpts {
        val driverOpts = DriverOpts()
        driverOpts.options.putAll(options)
        return driverOpts
    }
}
