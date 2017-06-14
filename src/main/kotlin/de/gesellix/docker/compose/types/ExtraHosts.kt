package de.gesellix.docker.compose.types

data class ExtraHosts(

        val entries: HashMap<String, String> = hashMapOf()
)
