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
        when (reader.peek()) {
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

    private fun parseServiceVolumeEntry(reader: JsonReader): List<ServiceVolume> {
        when (reader.peek()) {
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

                val volume = ServiceVolume()
                var buf = ""
                for (char in value) {
                    buf = if (isWindowsDrive(buf, char)) {
                        "$buf$char"
                    } else if (char == ':') {
                        populateFieldFromBuffer(false, buf, volume)
                        ""
                    } else {
                        "$buf$char"
                    }
                }
                populateFieldFromBuffer(true, buf, volume)

                populateType(volume)
                return listOf(volume)
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                val volume = ServiceVolume()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    when (reader.peek()) {
                        JsonReader.Token.STRING -> {
                            val value = reader.nextString()
                            writeProperty(volume, name, value)
                        }
                        JsonReader.Token.NUMBER -> {
                            val value = reader.nextInt()
                            writeProperty(volume, name, value)
                        }
                        JsonReader.Token.BEGIN_OBJECT -> {
                            reader.beginObject()
                            val volumeVolume = ServiceVolumeVolume()
                            while (reader.hasNext()) {
                                val name = reader.nextName()
                                when (reader.peek()) {
                                    JsonReader.Token.BOOLEAN -> {
                                        val value = reader.nextBoolean()
                                        writeProperty(volumeVolume, name, value)
                                    }
                                    else -> {
                                        // ...
                                    }
                                }
                            }
                            reader.endObject()
                            writeProperty(volume, name, volumeVolume)
                        }
                        else -> {
                            // ...
                        }
                    }
                }
                reader.endObject()
                populateType(volume)
                return listOf(volume)
            }
            else -> {
                // ...
            }
        }
        return listOf()
    }

    private fun isWindowsDrive(buf: String, c: Char): Boolean {
        return c == ':' && buf.length == 1 && buf[0].isLetter()

//        val firstTwoChars = s.slice(IntRange(0, 1))
//        return firstTwoChars.first().isLetter() && firstTwoChars.last() == ':'
    }

    private fun populateFieldFromBuffer(endOfInput: Boolean, buffer: String, volume: ServiceVolume) {
        if (!endOfInput && buffer.isEmpty()) {
            throw IllegalStateException("empty section between colons")
        }

        if (volume.source.isEmpty() && endOfInput) {
            volume.target = buffer
            return
        } else if (volume.source.isEmpty()) {
            volume.source = buffer
            return
        } else if (volume.target.isEmpty()) {
            volume.target = buffer
            return
        }

        if (!endOfInput) {
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

    private fun isBindOption(option: String): Boolean {
        return MountPropagation.values().find { it.propagation == option } != null
    }

    private fun populateType(volume: ServiceVolume) {
        if (volume.type.isNotEmpty()) {
            return
        }
        when {
            volume.source.isEmpty() -> {
                volume.type = ServiceVolumeType.TypeVolume.typeName
            }
            isFilePath(volume.source) -> {
                volume.type = ServiceVolumeType.TypeBind.typeName
            }
            else -> {
                volume.type = ServiceVolumeType.TypeVolume.typeName
            }
        }
    }

    private fun isFilePath(source: String): Boolean {
        if (listOf('.', '/', '~').contains(source.first())) {
            return true
        }

        val firstTwoChars = source.slice(IntRange(0, 1))
//        first, nextIndex : = utf8.DecodeRuneInString(source)
        return isWindowsDrive(firstTwoChars.first().toString(), firstTwoChars.last())
    }
}
