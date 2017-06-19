package de.gesellix.docker.compose.types

import de.gesellix.docker.compose.adapters.ExternalType
import de.gesellix.docker.compose.adapters.LabelsType

data class Secret(

        var file: String? = null,
        @ExternalType
        var external: External? = External(),
        @LabelsType
        var labels: Labels? = null
)
