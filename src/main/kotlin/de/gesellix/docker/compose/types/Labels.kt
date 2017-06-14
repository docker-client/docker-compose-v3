package de.gesellix.docker.compose.types

data class Labels(

        val entries: HashMap<String, String> = hashMapOf()
)
