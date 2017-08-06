package de.gesellix.docker.compose.types

// MountPropagation represents the propagation of a mount.
enum class MountPropagation(val propagation: String) {
    // PropagationRPrivate RPRIVATE
    PropagationRPrivate("rprivate"),
    // PropagationPrivate PRIVATE
    PropagationPrivate("private"),
    // PropagationRShared RSHARED
    PropagationRShared("rshared"),
    // PropagationShared SHARED
    PropagationShared("shared"),
    // PropagationRSlave RSLAVE
    PropagationRSlave("rslave"),
    // PropagationSlave SLAVE
    PropagationSlave("slave")
}
