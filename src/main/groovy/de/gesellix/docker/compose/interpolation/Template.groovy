package de.gesellix.docker.compose.interpolation

import com.google.re2j.Matcher
import com.google.re2j.Pattern

class Template {

    final String delimiter = '\\$'
    final String substitution = '[_a-z][_a-z0-9]*(?::?-[^}]+)?'
    final String patternString = "${delimiter}(?i:(?P<escaped>${delimiter})|(?P<named>${substitution})|{(?P<braced>${substitution})}|(?P<invalid>))"
    final Pattern pattern = Pattern.compile(patternString)

    String substitute(String input, Map<String, String> environment) {
        StringBuffer result = new StringBuffer()

        Matcher m = pattern.matcher(input)
        while (m.find()) {
            def substitution
            if (m.group(2) != null) {
                // named
                substitution = m.group(2)
            }
            else if (m.group(3) != null) {
                // braced
                substitution = m.group(3)
            }
            if (substitution != null) {
                // Soft default (fall back if unset or empty)
                if (substitution.contains(":-")) {
                    def (name, defaultValue) = partition(substitution, ":-")
                    def (value, ok) = lookupEnv(name as String, environment)
                    if (ok && value != "") {
                        m.appendReplacement(result, value as String)
                    }
                    else {
                        m.appendReplacement(result, defaultValue as String)
                    }
                    continue
                }

                // Hard default (fall back if-and-only-if empty)
                if (substitution.contains("-")) {
                    def (name, defaultValue) = partition(substitution, "-")
                    def (value, ok) = lookupEnv(name as String, environment)
                    if (ok) {
                        m.appendReplacement(result, value as String)
                    }
                    else {
                        m.appendReplacement(result, defaultValue as String)
                    }
                    continue
                }

                // No default (fall back to empty string)
                def (value, ok) = lookupEnv(substitution, environment)
                if (ok) {
                    m.appendReplacement(result, value as String)
                }
                else {
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
            throw new IllegalStateException("Invalid template: ${input}")
        }
        m.appendTail(result)

        return result
    }

    static def partition(String s, String sep) {
        if (s.contains(sep)) {
            def parts = s.split(sep, 2)
            return [parts[0], parts[1]]
        }
        return [s, ""]
    }

    static def lookupEnv(String name, Map<String, String> environment) {
        return [environment[name], environment.containsKey(name)]
    }
}
