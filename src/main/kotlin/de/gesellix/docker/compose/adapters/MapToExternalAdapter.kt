package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.External

class MapToExternalAdapter {

    @ToJson
    fun toJson(@ExternalType external: External): Map<String, String> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @ExternalType
    fun fromJson(reader: JsonReader): External {
        val external = External()
        val token = reader.peek()
        when (token) {
            JsonReader.Token.BOOLEAN -> {
                external.external = reader.nextBoolean()
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                val name = reader.nextName()
                val value = reader.nextString()
                external.external = true
                external.name = value
                reader.endObject()
            }
            else -> {
                // ...
            }
        }
        return external
    }
}
