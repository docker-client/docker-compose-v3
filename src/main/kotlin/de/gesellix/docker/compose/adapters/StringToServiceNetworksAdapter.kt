package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.ServiceNetwork

class StringToServiceNetworksAdapter {

    @ToJson
    fun toJson(@ServiceNetworksType networks: Map<String, ServiceNetwork>): List<String> {
        throw  UnsupportedOperationException()
    }

    @FromJson
    @ServiceNetworksType
    fun fromJson(reader: JsonReader): Map<String, ServiceNetwork?> {
        val result = hashMapOf<String, ServiceNetwork?>()
        val token = reader.peek()
        when (token) {
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    val valueType = reader.peek()
                    when (valueType) {
                        JsonReader.Token.NULL -> result[name] = reader.nextNull()
                        JsonReader.Token.STRING -> throw UnsupportedOperationException("didn't expect a String value for network $name")

                        //                    result[name] = reader.nextString()
                        JsonReader.Token.BEGIN_OBJECT -> {
                            val serviceNetwork = ServiceNetwork()
                            reader.beginObject()
                            while (reader.hasNext()) {
                                val attr = reader.nextName()
                                when (attr) {
                                    "ipv4_address" -> serviceNetwork.ipv4Address = reader.nextString()
                                    "ipv6_address" -> serviceNetwork.ipv6Address = reader.nextString()
                                    "aliases" -> {
                                        val aliases = arrayListOf<String>()
                                        reader.beginArray()
                                        while (reader.hasNext()) {
                                            aliases.add(reader.nextString())
                                        }
                                        reader.endArray()
                                        serviceNetwork.aliases = aliases
                                    }
                                    else -> {
                                        // ...
                                    }
                                }
                            }
                            reader.endObject()
                            result[name] = serviceNetwork
                        }
                        else -> {
                            // ...
                        }
                    }
                }
                reader.endObject()
            }
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    val name = reader.nextString()
                    //            def value = reader.nextNull()
                    result[name] = null
                }
                reader.endArray()
            }
            else -> {
                // ...
            }
        }
        return result
    }
}
