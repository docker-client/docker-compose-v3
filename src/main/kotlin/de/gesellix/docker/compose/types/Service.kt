package de.gesellix.docker.compose.types

import com.squareup.moshi.Json
import de.gesellix.docker.compose.adapters.CommandType
import de.gesellix.docker.compose.adapters.EnvironmentType
import de.gesellix.docker.compose.adapters.ExtraHostsType
import de.gesellix.docker.compose.adapters.LabelsType
import de.gesellix.docker.compose.adapters.PortConfigsType
import de.gesellix.docker.compose.adapters.ServiceNetworksType
import de.gesellix.docker.compose.adapters.ServiceSecretsType

data class Service(

        var build: Any? = null,
        @Json(name = "cap_add")
        var capAdd: Set<String>? = null,
        @Json(name = "cap_drop")
        var capDrop: Set<String>? = null,
        @Json(name = "cgroup_parent")
        var cgroupParent: String? = null,
        @CommandType
        var command: Command? = null,
        @Json(name = "container_name")
        var containerName: String? = null,
        @Json(name = "depends_on")
        var dependsOn: Set<String>? = null,
        var deploy: Deploy? = null,
        var devices: Set<String>? = null,
        var dns: List<String>? = null,
        @Json(name = "dns_search")
        var dnsSearch: List<String>? = null,
        var domainname: String? = null,
        var entrypoint: List<String>? = null,
        @Json(name = "env_file")
        var envFile: List<String>? = null,
        @EnvironmentType
        var environment: Environment = Environment(),
        var expose: Set<String>? = null,
        @Json(name = "external_links")
        var externalLinks: Set<String>? = null,
        @Json(name = "extra_hosts")
        @ExtraHostsType
        var extraHosts: ExtraHosts? = null,
        var healthcheck: Healthcheck? = null,
        var hostname: String? = null,
        var image: String? = null,
        var ipc: String? = null,
        @LabelsType
        var labels: Labels? = null,
        var links: Set<String>? = null,
        var logging: Logging? = null,
        @Json(name = "mac_address")
        var macAddress: String? = null,
        @Json(name = "network_mode")
        var networkMode: String? = null,
        @ServiceNetworksType
        var networks: Map<String, ServiceNetwork>? = null,
        var pid: String? = null,
        @PortConfigsType
        var ports: PortConfigs = PortConfigs(),
        var privileged: Boolean? = null,
        @Json(name = "read_only")
        var readOnly: Boolean? = null,
        var restart: String? = null,
        @Json(name = "security_opt")
        var securityOpt: Set<String>? = null,
        var shmSize: Float? = null,
        @ServiceSecretsType
        var secrets: List<Map<String, ServiceSecret>>? = null,
        var sysctls: Any? = null,
        @Json(name = "stdin_open")
        var stdinOpen: Boolean? = null,
        @Json(name = "stop_grace_period")
        var stopGracePeriod: String? = null,
        @Json(name = "stop_signal")
        var stopSignal: String? = null,
        var tmpfs: List<String>? = null,
        var tty: Boolean? = null,
        var ulimits: Ulimits? = null,
        var user: String? = null,
        @Json(name = "userns_mode")
        var usernsMode: String? = null,
        var volumes: Set<String>? = null,
        @Json(name = "working_dir")
        var workingDir: String? = null
)
