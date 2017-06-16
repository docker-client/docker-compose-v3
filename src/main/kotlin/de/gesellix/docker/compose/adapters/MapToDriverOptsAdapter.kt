package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.DriverOpts

class MapToDriverOptsAdapter {

    @ToJson
    fun toJson(@DriverOptsType driverOpts: DriverOpts): Map<String, String> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @DriverOptsType
    fun fromJson(options: Map<String, String>): DriverOpts {
        val driverOpts = DriverOpts()
        for ((key, value) in options) {
//            if (value is Int) {
//                driverOpts.options.put(key, value.toString())
//            } else
            if (value is String) {
                driverOpts.options.put(key, value)
            } else {
                throw IllegalStateException("expected driver_opts.$key to be either Int or String, but was ${value::class.java}")
            }
        }
        return driverOpts
    }
}
