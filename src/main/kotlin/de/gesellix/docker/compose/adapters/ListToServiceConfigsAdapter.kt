package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.ServiceConfig


class ListToServiceConfigsAdapter {


    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(@ServiceConfigsType configs: ArrayList<Map<String, ServiceConfig?>>): List<Map<String, Any>> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @ServiceConfigsType
    fun fromJson(reader: JsonReader): ArrayList<Map<String, ServiceConfig?>> {
        val result = arrayListOf<Map<String, ServiceConfig?>>()
        val token = reader.peek()
        when (token) {
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    result.addAll(parseServiceConfigEntry(reader))
                }
                reader.endArray()
            }
            else -> {
                // ...
            }
        }
        return result
    }

    private fun parseServiceConfigEntry(reader: JsonReader): List<Map<String, ServiceConfig?>> {
        val entryToken = reader.peek()
        if (entryToken == JsonReader.Token.STRING) {
            val value = reader.nextString()
            return listOf(mapOf(Pair(value, null)))
        } else if (entryToken == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject()
            val config = ServiceConfig()
            while (reader.hasNext()) {
                val name = reader.nextName()
                val valueType = reader.peek()
                when (valueType) {
                    JsonReader.Token.STRING -> {
                        val value = reader.nextString()
                        writePropery(config, name, value)
                    }
                    JsonReader.Token.NUMBER -> {
                        val value = reader.nextInt()
                        writePropery(config, name, value)
                    }
                    else -> {
                        // ...
                    }
                }
            }
            reader.endObject()
            return listOf(mapOf(Pair(config.source, config)))
        } else {
            // ...
        }
        return listOf()
    }

}
