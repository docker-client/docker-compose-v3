package de.gesellix.docker.compose.types

enum class ServiceVolumeType(val typeName: String) {

    // TypeBind is the type for mounting host dir
    TypeBind("bind"),

    // TypeVolume is the type for remote storage volumes
    TypeVolume("volume"),

    // TypeTmpfs is the type for mounting tmpfs
    TypeTmpfs("tmpfs")
}
