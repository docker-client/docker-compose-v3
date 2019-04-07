package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.ExtraHosts

class MapOrListToExtraHosts {

    @ToJson
    fun toJson(@ExtraHostsType extraHosts: ExtraHosts): Map<String, String> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @ExtraHostsType
    fun fromJson(reader: JsonReader): ExtraHosts {
        val extraHosts = ExtraHosts()
        val token = reader.peek()
        when (token) {
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.peek() != JsonReader.Token.END_ARRAY) {
                    val entry = reader.nextString()
                    val keyAndValue = entry.split(Regex(":"), 2)
                    extraHosts.entries[keyAndValue[0]] = if (keyAndValue.size > 1) keyAndValue[1] else ""
                }
                reader.endArray()
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                while (reader.peek() != JsonReader.Token.END_OBJECT) {
                    val name = reader.nextName()
                    val value: String? = if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
                    extraHosts.entries[name] = value ?: ""
                }
                reader.endObject()
            }
            else -> {
                // ...
            }
        }
        return extraHosts
    }
}
