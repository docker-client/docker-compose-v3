package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.MountPropagation
import de.gesellix.docker.compose.types.ServiceVolume
import de.gesellix.docker.compose.types.ServiceVolumeBind
import de.gesellix.docker.compose.types.ServiceVolumeType
import de.gesellix.docker.compose.types.ServiceVolumeVolume


class ListToServiceVolumesAdapter {

    @ToJson
    fun toJson(@ServiceVolumesType volumes: ArrayList<ServiceVolume>): List<Map<String, Any>> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @ServiceVolumesType
    fun fromJson(reader: JsonReader): ArrayList<ServiceVolume> {
        val result = arrayListOf<ServiceVolume>()
        val token = reader.peek()
        when (token) {
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    result.addAll(parseServiceVolumeEntry(reader))
                }
                reader.endArray()
            }
            else -> {
                // ...
            }
        }
        return result
    }

    fun parseServiceVolumeEntry(reader: JsonReader): List<ServiceVolume> {
        val entryToken = reader.peek()
        when (entryToken) {
            JsonReader.Token.STRING -> {
                val value = reader.nextString()
                if (value.isNullOrEmpty()) {
                    throw IllegalStateException("invalid empty volume spec")
                }
                if (value.length == 1 || value.length == 2) {
                    return listOf(ServiceVolume(
                            type = ServiceVolumeType.TypeVolume.typeName,
                            target = value))
                }

                val endOfSpec = '0'
                val spec = "$value$endOfSpec"

                val volume = ServiceVolume()
                var buf = ""
                for (char in spec) {
                    if (isWindowsDrive(buf, char)) {
                        buf = "$buf$char"
                    } else if (char == ':' || char == endOfSpec) {
                        populateFieldFromBuffer(char, buf, volume)
                        buf = ""
                    } else {
                        buf = "$buf$char"
                    }
                }

                populateType(volume)
                return listOf(volume)
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                val volume = ServiceVolume()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    val valueType = reader.peek()
                    when (valueType) {
                        JsonReader.Token.STRING -> {
                            val value = reader.nextString()
                            writePropery(volume, name, value)
                        }
                        JsonReader.Token.NUMBER -> {
                            val value = reader.nextInt()
                            writePropery(volume, name, value)
                        }
                        else -> {
                            // ...
                        }
                    }
                }
                reader.endObject()
                return listOf(volume)
            }
            else -> {
                // ...
            }
        }
        return listOf()
    }

    fun isWindowsDrive(buf: String, c: Char): Boolean {
        return c == ':' && buf.length == 1 && buf[0].isLetter()

//        val firstTwoChars = s.slice(IntRange(0, 1))
//        return firstTwoChars.first().isLetter() && firstTwoChars.last() == ':'
    }

    fun populateFieldFromBuffer(char: Char, buffer: String, volume: ServiceVolume) {
        if (buffer.isEmpty()) {
            throw IllegalStateException("empty section between colons")
        }

        if (volume.source.isEmpty() && char == '0') {
            volume.target = buffer
            return
        } else if (volume.source.isEmpty()) {
            volume.source = buffer
            return
        } else if (volume.target.isEmpty()) {
            volume.target = buffer
            return
        }

        if (char == ':') {
            throw IllegalStateException("too many colons")
        }

        val options = buffer.split(',')
        for (option in options) {
            if (option == "ro") {
                volume.readOnly = true
            }
            if (option == "rw") {
                volume.readOnly = false
            }
            if (option == "nocopy") {
                volume.volume = ServiceVolumeVolume(true)
            }
            if (isBindOption(option)) {
                volume.bind = ServiceVolumeBind(option)
            }
            // ignore unknown options
        }
    }

    fun isBindOption(option: String): Boolean {
        return MountPropagation.values().find { it.propagation == option } != null
    }

    fun populateType(volume: ServiceVolume) {
        if (volume.source.isEmpty()) {
            volume.type = ServiceVolumeType.TypeVolume.typeName
        } else if (isFilePath(volume.source)) {
            volume.type = ServiceVolumeType.TypeBind.typeName
        } else {
            volume.type = ServiceVolumeType.TypeVolume.typeName
        }
    }

    fun isFilePath(source: String): Boolean {
        if (listOf('.', '/', '~').contains(source.first())) {
            return true
        }

        val firstTwoChars = source.slice(IntRange(0, 1))
//        first, nextIndex : = utf8.DecodeRuneInString(source)
        return isWindowsDrive(firstTwoChars.first().toString(), firstTwoChars.last())
    }
}
