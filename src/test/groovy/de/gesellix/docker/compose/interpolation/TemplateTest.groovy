package de.gesellix.docker.compose.interpolation

import spock.lang.Specification
import spock.lang.Unroll

class TemplateTest extends Specification {

    def defaults = [
            "FOO": "first",
            "BAR": ""
    ]

    def template = new Template()

    def "test escaped"() {
        expect:
        template.substitute('$${foo}', defaults) == '${foo}'
    }

    @Unroll
    def "test invalid: #t"() {
        when:
        template.substitute(t, defaults)
        then:
        thrown(Exception)
        where:
        t << [
                '${',
                '$}',
                '${}',
                '${ }',
                '${ foo}',
                '${foo }',
                '${foo!}'
        ]
    }

    @Unroll
    def "test no value no default: #t"() {
        expect:
        template.substitute(t, defaults) == "This  var"
        where:
        t << [
                'This ${missing} var',
                'This ${BAR} var'
        ]
    }

    @Unroll
    def "test value no default: #t"() {
        expect:
        template.substitute(t, defaults) == "This first var"
        where:
        t << [
                'This $FOO var',
                'This ${FOO} var'
        ]
    }

    @Unroll
    def "test no value with default: #t"() {
        expect:
        template.substitute(t, defaults) == "ok def"
        where:
        t << [
                'ok ${missing:-def}',
                'ok ${missing-def}'
        ]
    }

    def "test empty value with soft default"() {
        expect:
        template.substitute('ok ${BAR:-def}', defaults) == "ok def"
    }

    def "test empty value with hard default"() {
        expect:
        template.substitute('ok ${BAR-def}', defaults) == "ok "
    }

    def "test non alphanumeric default"() {
        expect:
        template.substitute('ok ${BAR:-/non:-alphanumeric}', defaults) == "ok /non:-alphanumeric"
    }
}
