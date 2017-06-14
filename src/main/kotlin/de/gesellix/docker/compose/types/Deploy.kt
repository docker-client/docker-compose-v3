package de.gesellix.docker.compose.types

import com.squareup.moshi.Json
import de.gesellix.docker.compose.adapters.LabelsType

data class Deploy(

        val mode: String,
        val replicas: Int,
        @LabelsType
        val labels: Labels,
        @Json(name = "update_config")
        val updateConfig: UpdateConfig,
        val resources: Resources,
        @Json(name = "restart_policy")
        val restartPolicy: RestartPolicy,
        val placement: Placement,
        @Json(name = "endpoint_mode")
        val endpointMode: String
)
