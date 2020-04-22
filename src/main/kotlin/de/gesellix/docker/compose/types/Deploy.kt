package de.gesellix.docker.compose.types

import com.squareup.moshi.Json
import de.gesellix.docker.compose.adapters.LabelsType

data class Deploy(

        var mode: String? = null,
        var replicas: Int? = null,
        @LabelsType
        var labels: Labels? = null,
        @Json(name = "update_config")
        var updateConfig: UpdateConfig? = null,
        var resources: Resources? = null,
        @Json(name = "restart_policy")
        var restartPolicy: RestartPolicy? = null,
        var placement: Placement? = null,
        @Json(name = "endpoint_mode")
        var endpointMode: String? = null,
        @Json(name = "max_replicas_per_node")
        var maxReplicasPerNode: Int? = null
)
