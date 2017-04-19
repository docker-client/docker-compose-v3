package de.gesellix.docker.compose.interpolation

class ComposeInterpolator {

    Template template = new Template()

    def interpolate(Map<String, Map<String, Object>> composeContent, Map<String, String> environment) {
        def result = [:]
        if (composeContent['services']) {
            result['services'] = interpolateSection(composeContent.services, environment)
        }
        if (composeContent['networks']) {
            result['networks'] = interpolateSection(composeContent.networks, environment)
        }
        if (composeContent['volumes']) {
            result['volumes'] = interpolateSection(composeContent.volumes, environment)
        }
        if (composeContent['secrets']) {
            result['secrets'] = interpolateSection(composeContent.secrets, environment)
        }
        return result
    }

    def interpolateSection(Map<String, Object> config, Map<String, String> environment) {
        Map<String, Object> result = [:]
        config.each { key, value ->
            if (value == null) {
                result[key] = null
            }
            else {
                Map<String, Object> item
                try {
                    item = value as Map<String, Object>
                }
                catch (Exception e) {
                    throw new IllegalStateException("Invalid type for ${key}: ${value.class} instead of ${result.class}", e)
                }
                result[key] = interpolateSectionItem(key, item, environment)
            }
        }
        return result
    }

    def interpolateSectionItem(String name, Map<String, Object> config, Map<String, String> environment) {
        Map<String, Object> result = [:]
        config.each { key, value ->
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

    def recursiveInterpolate(def value, Map<String, String> environment) {
        if (value instanceof String) {
            return template.substitute(value, environment)
        }
        else if (value instanceof Map<String, Object>) {
            return (value as Map<String, Object>).collectEntries { key, elem ->
                return [(key): recursiveInterpolate(elem, environment)]
            }
        }
        else if (value instanceof List) {
            return (value as List).collect { elem ->
                return recursiveInterpolate(elem, environment)
            }
        }
        else {
            return value
        }
    }
}
