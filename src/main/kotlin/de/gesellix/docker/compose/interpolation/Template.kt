package de.gesellix.docker.compose.interpolation

import com.google.re2j.Pattern

class Template {

    val delimiter = "\\$"
    val substitution = "[_a-z][_a-z0-9]*(?::?-[^}]+)?"
    val patternString = "$delimiter(?i:(?P<escaped>$delimiter)|(?P<named>$substitution)|{(?P<braced>$substitution)}|(?P<invalid>))"
    val pattern: Pattern = Pattern.compile(patternString)

    fun substitute(input: String, environment: Map<String, String>): String {
        val result = StringBuffer()

        val m = pattern.matcher(input)
        while (m.find()) {
            var substitution: String? = null
            if (m.group(2) != null) {
                // named
                substitution = m.group(2)
            } else if (m.group(3) != null) {
                // braced
                substitution = m.group(3)
            }
            if (substitution != null) {
                // Soft default (fall back if unset or empty)
                if (substitution.contains(":-")) {
                    val (name, defaultValue) = partition(substitution, Regex(":-"))
                    val (value, ok) = lookupEnv(name, environment)
                    if (ok && value != "") {
                        m.appendReplacement(result, value as String)
                    } else {
                        m.appendReplacement(result, defaultValue)
                    }
                    continue
                }

                // Hard default (fall back if-and-only-if empty)
                if (substitution.contains("-")) {
                    val (name, defaultValue) = partition(substitution, Regex("-"))
                    val (value, ok) = lookupEnv(name, environment)
                    if (ok) {
                        m.appendReplacement(result, value as String)
                    } else {
                        m.appendReplacement(result, defaultValue)
                    }
                    continue
                }

                // No default (fall back to empty string)
                val (value, ok) = lookupEnv(substitution, environment)
                if (ok) {
                    m.appendReplacement(result, value as String)
                } else {
                    m.appendReplacement(result, "")
                }
                continue
            }

            if (m.group(1) != null) {
                // escaped
                m.appendReplacement(result, m.group(1))
                continue
            }

            // invalid
            throw IllegalStateException("Invalid template: $input")
        }
        m.appendTail(result)

        return result.toString()
    }

    data class Partitions(val left: String, val right: String)

    fun partition(s: String, sep: Regex): Partitions {
        if (s.contains(sep)) {
            val parts = s.split(sep, 2)
            return Partitions(parts[0], parts[1])
        }
        return Partitions(s, "")
    }

    data class Result(val result: String?, val found: Boolean)

    fun lookupEnv(name: String, environment: Map<String, String>): Result {
        return Result(environment[name], environment.containsKey(name))
    }
}
