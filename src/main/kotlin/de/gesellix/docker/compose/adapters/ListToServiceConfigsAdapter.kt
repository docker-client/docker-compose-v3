package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.ServiceConfig

class ListToServiceConfigsAdapter {

    @ToJson
    fun toJson(@ServiceConfigsType configs: ArrayList<Map<String, ServiceConfig?>>): List<Map<String, Any>> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @ServiceConfigsType
    fun fromJson(reader: JsonReader): ArrayList<Map<String, ServiceConfig?>> {
        val result = arrayListOf<Map<String, ServiceConfig?>>()
        when (reader.peek()) {
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
        when (reader.peek()) {
            JsonReader.Token.STRING -> {
                val value = reader.nextString()
                return listOf(mapOf(Pair(value, null)))
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                val config = ServiceConfig()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    when (reader.peek()) {
                        JsonReader.Token.STRING -> {
                            val value = reader.nextString()
                            writeProperty(config, name, value)
                        }
                        JsonReader.Token.NUMBER -> {
                            val value = reader.nextInt()
                            writeProperty(config, name, value)
                        }
                        else -> {
                            // ...
                        }
                    }
                }
                reader.endObject()
                return listOf(mapOf(Pair(config.source, config)))
            }
            else -> {
                // ...
            }
        }
        return listOf()
    }
}
