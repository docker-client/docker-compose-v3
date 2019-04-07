package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.Command

class StringOrListToCommandAdapter {

    @ToJson
    fun toJson(@CommandType command: Command): List<String> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @CommandType
    fun fromJson(reader: JsonReader): Command {
        val command = Command()
        val token = reader.peek()
        when (token) {
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    command.parts.add(reader.nextString())
                }
                reader.endArray()
            }
            JsonReader.Token.STRING -> command.parts.add(reader.nextString())
            else -> {
                // ...
            }
        }
        return command
    }
}
