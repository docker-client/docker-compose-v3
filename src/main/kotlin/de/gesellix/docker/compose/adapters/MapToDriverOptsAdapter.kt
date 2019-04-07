package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.DriverOpts

class MapToDriverOptsAdapter {

    @ToJson
    fun toJson(@DriverOptsType driverOpts: DriverOpts): Map<String, String> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @DriverOptsType
    fun fromJson(reader: JsonReader): DriverOpts {
        val driverOpts = DriverOpts()
        val token = reader.peek()
        when (token) {
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                while (reader.peek() != JsonReader.Token.END_OBJECT) {
                    val name = reader.nextName()
                    val value: String? = if (reader.peek() == JsonReader.Token.NULL) {
                        reader.nextNull()
                    } else if (reader.peek() == JsonReader.Token.NUMBER) {
                        val d = reader.nextDouble()
                        if ((d % 1) == 0.0) {
                            d.toInt().toString()
                        } else {
                            d.toString()
                        }
                    } else {
                        reader.nextString()
                    }
                    driverOpts.options[name] = value ?: ""
                }
                reader.endObject()
            }
            else -> {
                // ...
            }
        }
        return driverOpts
    }
}
