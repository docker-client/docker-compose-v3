package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.Entrypoint

class StringOrListToEntrypointAdapter {

    @ToJson
    fun toJson(@EntrypointType entrypoint: Entrypoint): List<String> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @EntrypointType
    fun fromJson(reader: JsonReader): Entrypoint {
        val entrypoint = Entrypoint()
        when (reader.peek()) {
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    entrypoint.parts.add(reader.nextString())
                }
                reader.endArray()
            }
            JsonReader.Token.STRING -> {
                entrypoint.parts.add(reader.nextString())
            }
            else -> {
                // ...
            }
        }
        return entrypoint
    }
}
