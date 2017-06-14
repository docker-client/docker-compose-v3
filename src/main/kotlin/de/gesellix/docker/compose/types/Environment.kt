package de.gesellix.docker.compose.types

data class Environment(

        var entries: HashMap<String, String> = hashMapOf()
)
