package de.gesellix.docker.compose

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.gesellix.docker.compose.adapters.ListToExposeAdapter
import de.gesellix.docker.compose.adapters.ListToPortConfigsAdapter
import de.gesellix.docker.compose.adapters.ListToServiceSecretsAdapter
import de.gesellix.docker.compose.adapters.ListToServiceVolumesAdapter
import de.gesellix.docker.compose.adapters.MapOrListToEnvironmentAdapter
import de.gesellix.docker.compose.adapters.MapOrListToExtraHosts
import de.gesellix.docker.compose.adapters.MapOrListToLabelAdapter
import de.gesellix.docker.compose.adapters.MapToDriverOptsAdapter
import de.gesellix.docker.compose.adapters.MapToExternalAdapter
import de.gesellix.docker.compose.adapters.StringOrListToCommandAdapter
import de.gesellix.docker.compose.adapters.StringToServiceNetworksAdapter
import de.gesellix.docker.compose.interpolation.ComposeInterpolator
import de.gesellix.docker.compose.types.ComposeConfig
import mu.KotlinLogging
import org.yaml.snakeyaml.Yaml
import java.io.InputStream

private val log = KotlinLogging.logger {}

class ComposeFileReader {

    // UnsupportedProperties not yet supported by this implementation of the compose file
    val UnsupportedProperties = listOf(
            "build",
            "cap_add",
            "cap_drop",
            "cgroup_parent",
            "devices",
            "dns",
            "dns_search",
            "domainname",
            "external_links",
            "ipc",
            "links",
            "mac_address",
            "network_mode",
            "privileged",
            "read_only",
            "restart",
            "security_opt",
            "shm_size",
            "sysctls",
            "tmpfs",
            "userns_mode")

    // DeprecatedProperties that were removed from the v3 format, but their use should not impact the behaviour of the application.
    val DeprecatedProperties = hashMapOf<String, String>().let {
        it["container_name"] = "Setting the container name is not supported."
        it["expose"] = "Exposing ports is unnecessary - services on the same network can access each other's containers on any port."
        it
    }

    // ForbiddenProperties that are not supported in this implementation of the compose file.
    val ForbiddenProperties = hashMapOf<String, String>().let {
        it["extends"] = "Support for `extends` is not implemented yet."
        it["volume_driver"] = "Instead of setting the volume driver on the service, define a volume using the top-level `volumes` option and specify the driver there."
        it["volumes_from"] = "To share a volume between services, define it using the top-level `volumes` option and reference it from each service that shares it using the service-level `volumes` option."
        it["cpu_quota"] = "Set resource limits using deploy.resources"
        it["cpu_shares"] = "Set resource limits using deploy.resources"
        it["cpuset"] = "Set resource limits using deploy.resources"
        it["mem_limit"] = "Set resource limits using deploy.resources"
        it["memswap_limit"] = "Set resource limits using deploy.resources"
        it
    }

    val interpolator = ComposeInterpolator()

    fun loadYaml(composeFile: InputStream): HashMap<String, Map<String, Map<String, Any?>?>> {
        val composeContent = Yaml().loadAs(composeFile, Map::class.java) as Map<String, Map<String, Map<String, Any?>?>>
        log.info("composeContent: $composeContent}")

        return hashMapOf<String, Map<String, Map<String, Any?>?>>().apply {
            putAll(composeContent)
        }
    }

    fun load(composeFile: InputStream, workingDir: String, environment: Map<String, String> = System.getenv()): ComposeConfig? {
        val composeContent = loadYaml(composeFile)

        val forbiddenProperties = collectForbiddenServiceProperties(composeContent["services"], ForbiddenProperties)
        if (forbiddenProperties.isNotEmpty()) {
            log.error("Configuration contains forbidden properties: $forbiddenProperties")
            throw IllegalStateException("Configuration contains forbidden properties")
        }

        // overrides interpolated sections, but keeps non-interpolated ones.
        composeContent.putAll(interpolator.interpolate(composeContent, environment))

        val cfg = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(ListToExposeAdapter())
                .add(ListToPortConfigsAdapter())
                .add(ListToServiceSecretsAdapter())
                .add(ListToServiceVolumesAdapter())
                .add(MapOrListToEnvironmentAdapter())
                .add(MapOrListToExtraHosts())
                .add(MapOrListToLabelAdapter())
                .add(MapToDriverOptsAdapter())
                .add(MapToExternalAdapter())
                .add(StringOrListToCommandAdapter())
                .add(StringToServiceNetworksAdapter())
                .build()
                .adapter(ComposeConfig::class.java)
                .fromJsonValue(composeContent)

//        def valid = new SchemaValidator().validate(composeContent)

        val unsupportedProperties = collectUnsupportedServiceProperties(composeContent["services"], UnsupportedProperties)
        if (unsupportedProperties.isNotEmpty()) {
            log.warn("Ignoring unsupported options: ${unsupportedProperties.joinToString(", ")}")
        }

        val deprecatedProperties = collectDeprecatedServiceProperties(composeContent["services"], DeprecatedProperties)
        if (deprecatedProperties.isNotEmpty()) {
            log.warn("Ignoring deprecated options: $deprecatedProperties")
        }

//        if volume.External.Name == "" {
//            volume.External.Name = name
//            volumes[name] = volume
//        } else {
//            logrus.Warnf("volume %s: volume.external.name is deprecated in favor of volume.name", name)
//
//            if volume.Name != "" {
//                return nil, errors.Errorf("volume %s: volume.external.name and volume.name conflict; only use volume.name", name)
//            }
//        }

        return cfg
    }

    fun collectForbiddenServiceProperties(services: Map<String, Map<String, Any?>?>?, forbiddenProperties: Map<String, String>): Map<String, String> {
        val hits = hashMapOf<String, String>()
        services?.forEach { service, serviceConfig ->
            if (serviceConfig != null) {
                forbiddenProperties.forEach { property, description ->
                    if (serviceConfig.containsKey(property)) {
                        hits["$service.$property"] = description
                    }
                }
            }
        }
        return hits
    }

    fun collectUnsupportedServiceProperties(services: Map<String, Map<String, Any?>?>?, unsupportedProperties: List<String>): List<String> {
        val hits = arrayListOf<String>()
        services?.forEach { service, serviceConfig ->
            if (serviceConfig != null) {
                unsupportedProperties.forEach { property ->
                    if (serviceConfig.containsKey(property)) {
                        hits.add("$service.$property")
                    }
                }
            }
        }
        return hits
    }

    fun collectDeprecatedServiceProperties(services: Map<String, Map<String, Any?>?>?, deprecatedProperties: Map<String, String>): Map<String, String> {
        val hits = hashMapOf<String, String>()
        services?.forEach { service, serviceConfig ->
            if (serviceConfig != null) {
                deprecatedProperties.forEach { property, description ->
                    if (serviceConfig.containsKey(property)) {
                        hits["$service.$property"] = description
                    }
                }
            }
        }
        return hits
    }
}
