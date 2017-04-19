package de.gesellix.docker.compose

import com.squareup.moshi.Moshi
import de.gesellix.docker.compose.adapters.ListToPortConfigsAdapter
import de.gesellix.docker.compose.adapters.ListToServiceSecretsAdapter
import de.gesellix.docker.compose.adapters.MapOrListToEnvironmentAdapter
import de.gesellix.docker.compose.adapters.MapOrListToExtraHosts
import de.gesellix.docker.compose.adapters.MapOrListToLabelAdapter
import de.gesellix.docker.compose.adapters.MapToDriverOptsAdapter
import de.gesellix.docker.compose.adapters.MapToExternalAdapter
import de.gesellix.docker.compose.adapters.StringOrListToCommandAdapter
import de.gesellix.docker.compose.adapters.StringToServiceNetworksAdapter
import de.gesellix.docker.compose.interpolation.ComposeInterpolator
import de.gesellix.docker.compose.types.ComposeConfig
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml

@Slf4j
class ComposeFileReader {

    // UnsupportedProperties not yet supported by this implementation of the compose file
    def UnsupportedProperties = [
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
            "stop_signal",
            "sysctls",
            "tmpfs",
            "userns_mode",
    ]

    // DeprecatedProperties that were removed from the v3 format, but their use should not impact the behaviour of the application.
    def DeprecatedProperties = [
            "container_name": "Setting the container name is not supported.",
            "expose"        : "Exposing ports is unnecessary - services on the same network can access each other's containers on any port.",
    ]

    // ForbiddenProperties that are not supported in this implementation of the compose file.
    def ForbiddenProperties = [
            "extends"      : "Support for `extends` is not implemented yet.",
            "volume_driver": "Instead of setting the volume driver on the service, define a volume using the top-level `volumes` option and specify the driver there.",
            "volumes_from" : "To share a volume between services, define it using the top-level `volumes` option and reference it from each service that shares it using the service-level `volumes` option.",
            "cpu_quota"    : "Set resource limits using deploy.resources",
            "cpu_shares"   : "Set resource limits using deploy.resources",
            "cpuset"       : "Set resource limits using deploy.resources",
            "mem_limit"    : "Set resource limits using deploy.resources",
            "memswap_limit": "Set resource limits using deploy.resources",
    ]

    ComposeInterpolator interpolator = new ComposeInterpolator()

    Map<String, Map<String, Object>> loadYaml(InputStream composeFile) {
        Map<String, Map<String, Object>> composeContent = new Yaml().load(composeFile) as Map
        log.info("composeContent: $composeContent}")

        return composeContent
    }

    ComposeConfig load(InputStream composeFile, String workingDir, Map<String, String> environment = System.getenv()) {
        Map<String, Map<String, Object>> composeContent = loadYaml(composeFile)

        def forbiddenProperties = collectForbiddenServiceProperties(composeContent.services, ForbiddenProperties)
        if (forbiddenProperties) {
            log.error("Configuration contains forbidden properties: ${forbiddenProperties}")
            throw new IllegalStateException("Configuration contains forbidden properties")
        }

        // overrides interpolated sections
        composeContent.putAll(interpolator.interpolate(composeContent, environment))

        def json = JsonOutput.toJson(composeContent)

        ComposeConfig cfg = new Moshi.Builder()
                .add(new ListToPortConfigsAdapter())
                .add(new ListToServiceSecretsAdapter())
                .add(new MapOrListToEnvironmentAdapter())
                .add(new MapOrListToExtraHosts())
                .add(new MapOrListToLabelAdapter())
                .add(new MapToDriverOptsAdapter())
                .add(new MapToExternalAdapter())
                .add(new StringOrListToCommandAdapter())
                .add(new StringToServiceNetworksAdapter())
                .build()
                .adapter(ComposeConfig)
                .fromJson(json)

//        def valid = new SchemaValidator().validate(composeContent)

        def unsupportedProperties = collectUnsupportedServiceProperties(composeContent.services, UnsupportedProperties)
        if (unsupportedProperties) {
            log.warn("Ignoring unsupported options: ${unsupportedProperties.join(", ")}")
        }

        def deprecatedProperties = collectDeprecatedServiceProperties(composeContent.services, DeprecatedProperties)
        if (deprecatedProperties) {
            log.warn("Ignoring deprecated options: ${deprecatedProperties}")
        }

        return cfg
    }

    def collectForbiddenServiceProperties(services, Map<String, String> forbiddenProperties) {
        def hits = [:]
        services.each { service, serviceConfig ->
            forbiddenProperties.each { property, description ->
                if (serviceConfig[property]) {
                    hits["$service.$property"] = description
                }
            }
        }
        return hits
    }

    def collectUnsupportedServiceProperties(services, List<String> unsupportedProperties) {
        def hits = []
        services.each { service, serviceConfig ->
            unsupportedProperties.each { property ->
                if (serviceConfig[property]) {
                    hits["$service.$property"]
                }
            }
        }
        return hits
    }

    def collectDeprecatedServiceProperties(services, Map<String, String> deprecatedProperties) {
        def hits = [:]
        services.each { service, serviceConfig ->
            deprecatedProperties.each { property, description ->
                if (serviceConfig[property]) {
                    hits["$service.$property"] = description
                }
            }
        }
        return hits
    }
}
