package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.Exposes

class ListToExposeAdapter {

    @ToJson
    fun toJson(@ExposesType exposes: Exposes): List<String> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @ExposesType
    fun fromJson(reader: JsonReader): Exposes {
        val exposes = Exposes()
        val token = reader.peek()
        if (token == JsonReader.Token.BEGIN_ARRAY) {
            reader.beginArray()
            while (reader.hasNext()) {
                if (reader.peek() == JsonReader.Token.NUMBER) {
                    exposes.entries.add(reader.nextInt().toString())
                } else {
                    exposes.entries.add(reader.nextString())
                }
            }
            reader.endArray()
        } else {
            // ...
        }
        return exposes
    }
}
