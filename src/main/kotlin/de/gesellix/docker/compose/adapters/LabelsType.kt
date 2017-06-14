package de.gesellix.docker.compose.adapters

import com.squareup.moshi.JsonQualifier

@Retention(value = AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class LabelsType
