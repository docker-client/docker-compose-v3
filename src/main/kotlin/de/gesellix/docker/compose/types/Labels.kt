package de.gesellix.docker.compose.types

data class Labels(

        var entries: HashMap<String, String> = hashMapOf()
)
