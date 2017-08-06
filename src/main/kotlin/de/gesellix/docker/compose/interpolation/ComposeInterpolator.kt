package de.gesellix.docker.compose.interpolation

class ComposeInterpolator {

    val template = Template()

    fun interpolate(composeContent: Map<String, Map<String, Map<String, Any?>?>?>, environment: Map<String, String>): Map<String, Map<String, Map<String, Any?>?>> {
        val result = hashMapOf<String, Map<String, Map<String, Any?>?>>()
        listOf("services", "networks", "volumes", "secrets", "configs").forEach { section ->
            val sectionConfig = composeContent[section]
            if (sectionConfig != null) {
                result[section] = interpolateSection(sectionConfig, environment)
            }
        }
        return result
    }

    fun interpolateSection(config: Map<String, Map<String, Any?>?>, environment: Map<String, String>): Map<String, Map<String, Any?>?> {
        val result = hashMapOf<String, Map<String, Any?>?>()
        config.forEach { key, value ->
            if (value == null) {
                result[key] = null
            } else {
                try {
                    result[key] = interpolateSectionItem(key, value, environment)
                } catch (e: Exception) {
                    throw  IllegalStateException("Invalid type for $key: ${value.javaClass} instead of ${result.javaClass}", e)
                }
            }
        }
        return result
    }

    fun interpolateSectionItem(name: String, config: Map<String, Any?>, environment: Map<String, String>): Map<String, Any?> {
        val result = hashMapOf<String, Any?>()
        config.forEach { key, value ->
            result[key] = recursiveInterpolate(value, environment)
//            if err != nil {
//                return nil, errors.Errorf(
//                        "Invalid interpolation format for %#v option in %s %#v: %#v. You may need to escape any $ with another $.",
//                        key, section, name, err.Template,
//                        )
//            }
        }
        return result
    }

    fun recursiveInterpolate(value: Any?, environment: Map<String, String>): Any? {
        if (value == null) {
            return null
        } else if (value is String) {
            return template.substitute(value, environment)
        } else if (value is Map<*, *>) {
            val interpolatedMap = hashMapOf<Any, Any?>()
            value.forEach { key, elem ->
                if (key != null) {
                    interpolatedMap[key] = recursiveInterpolate(elem, environment)
                }
            }
            return interpolatedMap
        } else if (value is List<*>) {
            val interpolatedList = arrayListOf<Any?>()
            value.forEach { elem ->
                interpolatedList.add(recursiveInterpolate(elem, environment))
            }
            return interpolatedList
        } else {
            return value
        }
    }
}
