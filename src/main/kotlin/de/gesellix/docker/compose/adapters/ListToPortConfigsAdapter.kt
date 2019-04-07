package de.gesellix.docker.compose.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import de.gesellix.docker.compose.types.PortConfig
import de.gesellix.docker.compose.types.PortConfigs
import java.net.InetAddress

class ListToPortConfigsAdapter {

    @ToJson
    fun toJson(@PortConfigsType portConfigs: PortConfigs): List<Map<String, Any>> {
        throw UnsupportedOperationException()
    }

    @FromJson
    @PortConfigsType
    fun fromJson(reader: JsonReader): PortConfigs {
        val portConfigs = arrayListOf<PortConfig>()
        val token = reader.peek()
        when (token) {
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    portConfigs.addAll(parsePortConfigEntry(reader))
                }
                reader.endArray()
            }
            else -> {
                // ...
            }
        }
        return PortConfigs(portConfigs = portConfigs)
    }

    fun parsePortConfigEntry(reader: JsonReader): List<PortConfig> {
        val entryToken = reader.peek()
        when (entryToken) {
            JsonReader.Token.NUMBER -> {
                val value = Integer.toString(reader.nextInt())
                return parsePortDefinition(value)
            }
            JsonReader.Token.STRING -> {
                val value = reader.nextString()
                return parsePortDefinition(value)
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                val portConfig = PortConfig(mode = "", protocol = "", target = 0, published = 0)
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    val valueType = reader.peek()
                    when (valueType) {
                        JsonReader.Token.STRING -> {
                            val value = reader.nextString()
                            writePropery(portConfig, name, value)
                        }
                        JsonReader.Token.NUMBER -> {
                            val value = reader.nextInt()
                            writePropery(portConfig, name, value)
                        }
                        else -> {
                            // ...
                        }
                    }
                }
                reader.endObject()
                return listOf(portConfig)
            }
            else -> {
                // ...
            }
        }
        return emptyList()
    }

    fun parsePortDefinition(portSpec: String): List<PortConfig> {
        var (rawIP, hostPort, containerPort) = splitParts(portSpec)
        val (proto, plainContainerPort) = splitProto(containerPort)

        if (plainContainerPort.isNullOrEmpty()) {
            throw IllegalStateException("No port specified: '$portSpec'")
        }

        validateProto(proto)

        if (rawIP.isNotBlank()) {
            val address = InetAddress.getByName(rawIP)
            rawIP = address.hostAddress
        }

        val (startPort, endPort) = parsePortRange(plainContainerPort)

        var (startHostPort, endHostPort) = listOf(0, 0)
        if (hostPort.isNotEmpty()) {
            val (parsedStart, parsedEnd) = parsePortRange(hostPort)

            if ((endPort - startPort) != (parsedEnd - parsedStart)) {
                // Allow host port range if containerPort is not a range.
                // In this case, use the host port range as the dynamic
                // host port range to allocate into.
                if (endPort != startPort) {
                    throw  IllegalStateException("Invalid ranges specified for container and host Ports: '$containerPort' and '$hostPort'")
                }
            }
            startHostPort = parsedStart
            endHostPort = parsedEnd
        }

        val portMappings = arrayListOf<Map<String, Any>>()
        for (i in (0..(endPort - startPort))) {
            containerPort = "${startPort + i}"
            if (hostPort.isNotEmpty()) {
                hostPort = "${startHostPort + i}"
            }
            // Set hostPort to a range only if there is a single container port
            // and a dynamic host port.
            if (startPort == endPort && startHostPort != endHostPort) {
                hostPort = "$hostPort-$endHostPort"
            }
            val port = newPort(proto.toLowerCase(), containerPort)
            portMappings.add(hashMapOf<String, Any>().let { mapping ->
                mapping.put("port", port)
                mapping.put("binding", hashMapOf<String, Any>().let { binding ->
                    binding.put("proto", proto.toLowerCase())
                    binding.put("hostIP", rawIP)
                    binding.put("hostPort", hostPort)
                    binding
                })
                mapping
            })
        }

        val exposedPorts = sortedSetOf<String>()
        val bindings = hashMapOf<String, ArrayList<Map<String, Any?>>>()

        portMappings.forEach { portMapping ->
            val port = portMapping["port"] as String
            exposedPorts.add(port)

            if (!bindings.containsKey(port)) {
                bindings[port] = arrayListOf()
            }
            bindings[port]?.add(portMapping["binding"] as Map<String, Any?>)
        }

        val portConfigs = arrayListOf<PortConfig>()
        exposedPorts.forEach { port ->
            bindings[port]?.forEach { binding ->
                var hostPortAsInt = 0
                if (binding["hostPort"] != "") {
                    hostPortAsInt = binding["hostPort"]?.toString()?.toInt()!!
                }
                portConfigs.add(PortConfig(
                        protocol = binding["proto"].toString(),
                        target = port.split('/')[0].toInt(),
                        published = hostPortAsInt,
                        mode = "ingress"
                ))
            }
        }
        return portConfigs
    }

    // newPort creates a new instance of a port String given a protocol and port number or port range
    fun newPort(proto: String, port: String): String {
        // Check for parsing issues on "port" now so we can avoid having
        // to check it later on.

        var (portStartInt, portEndInt) = listOf(0, 0)
        if (port.isNotEmpty()) {
            val (parsedStart, parsedEnd) = parsePortRange(port)
            portStartInt = parsedStart
            portEndInt = parsedEnd
        }

        if (portStartInt == portEndInt) {
            return "$portStartInt/$proto"
        }
        return "$portStartInt-$portEndInt/$proto"
    }

    fun parsePortRange(ports: String): List<Int> {
        if (!ports.contains('-')) {
            return listOf(ports.toInt(), ports.toInt())
        }

        val startAndEnd = ports.split('-')
        val start = startAndEnd[0].toInt()
        val end = startAndEnd[1].toInt()
        if (end < start) {
            throw IllegalStateException("Invalid range specified for the Port '$ports'")
        }
        return listOf(start, end)
    }

    fun validateProto(proto: String) {
        if (!listOf("tcp", "udp").contains(proto.toLowerCase())) {
            throw  IllegalStateException("Invalid proto '$proto'")
        }
    }

    fun splitProto(rawPort: String): List<String> {
        val parts = rawPort.split('/')
        if (rawPort.isEmpty() || parts.isEmpty() || parts[0].isEmpty()) {
            return listOf("", "")
        }

        if (parts.size == 1) {
            return listOf("tcp", rawPort)
        }
        if (parts[1].isEmpty()) {
            return listOf("tcp", parts[0])
        }
        return listOf(parts[1], parts[0])
    }

    fun splitParts(rawPort: String): List<String> {
        val parts = rawPort.split(':')
        return when (parts.size) {
            1 -> listOf("", "", parts[0])
            2 -> listOf("", parts[0], parts[1])
            3 -> listOf(parts[0], parts[1], parts[2])
            else -> listOf(parts.take(parts.size - 2).joinToString(":"), parts[parts.size - 2], "${parts.size - 1}")
        }
    }
}
