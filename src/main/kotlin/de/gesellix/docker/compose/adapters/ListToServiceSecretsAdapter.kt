package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.ServiceSecret

class ListToServiceSecretsAdapter {

    @ToJson
    fun toJson(@ServiceSecretsType secrets: ArrayList<Map<String, ServiceSecret?>>): List<Map<String, Any>> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @ServiceSecretsType
    fun fromJson(reader: JsonReader): ArrayList<Map<String, ServiceSecret?>> {
        val result = arrayListOf<Map<String, ServiceSecret?>>()
        when (reader.peek()) {
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    result.addAll(parseServiceSecretEntry(reader))
                }
                reader.endArray()
            }
            else -> {
                // ...
            }
        }
        return result
    }

    private fun parseServiceSecretEntry(reader: JsonReader): List<Map<String, ServiceSecret?>> {
        when (reader.peek()) {
            JsonReader.Token.STRING -> {
                val value = reader.nextString()
                return listOf(mapOf(Pair(value, null)))
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                val secret = ServiceSecret()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    when (reader.peek()) {
                        JsonReader.Token.STRING -> {
                            val value = reader.nextString()
                            writeProperty(secret, name, value)
                        }
                        JsonReader.Token.NUMBER -> {
                            val value = reader.nextInt()
                            writeProperty(secret, name, value)
                        }
                        else -> {
                            // ...
                        }
                    }
                }
                reader.endObject()
                return listOf(mapOf(Pair(secret.source, secret)))
            }
            else -> {
                // ...
            }
        }
        return listOf()
    }
}
