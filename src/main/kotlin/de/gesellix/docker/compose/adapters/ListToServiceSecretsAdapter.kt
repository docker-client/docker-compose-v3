package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.ServiceSecret

class ListToServiceSecretsAdapter {

    @ToJson
    fun toJson(@ServiceSecretsType secrets: List<Map<String, ServiceSecret>>): List<Map<String, Any>> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @ServiceSecretsType
    fun fromJson(reader: JsonReader): List<Map<String, ServiceSecret?>> {
        val result = arrayListOf<Map<String, ServiceSecret?>>()
        val token = reader.peek()
        if (token == JsonReader.Token.BEGIN_ARRAY) {
            reader.beginArray()
            while (reader.hasNext()) {
                result.addAll(parseServiceSecretEntry(reader))
            }
            reader.endArray()
        } else {
            // ...
        }
        return result
    }

    fun parseServiceSecretEntry(reader: JsonReader): List<Map<String, ServiceSecret?>> {
        val entryToken = reader.peek()
        if (entryToken == JsonReader.Token.STRING) {
            val value = reader.nextString()
            return listOf(mapOf(Pair(value, null)))
        } else if (entryToken == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject()
            val secret = ServiceSecret()
            while (reader.hasNext()) {
                val name = reader.nextName()
                val valueType = reader.peek()
                if (valueType == JsonReader.Token.STRING) {
                    val value = reader.nextString()
                    writePropery(secret, name, value)
                } else if (valueType == JsonReader.Token.NUMBER) {
                    val value = reader.nextInt()
                    writePropery(secret, name, value)
                } else {
                    // ...
                }
            }
            reader.endObject()
            return listOf(mapOf(Pair(secret.source, secret)))
        } else {
            // ...
        }
        return listOf()
    }
}
