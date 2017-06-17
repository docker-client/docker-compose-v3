package de.gesellix.docker.compose.interpolation

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TemplateTest : Spek({

    val defaults = hashMapOf<String, String>().apply {
        put("FOO", "first")
        put("BAR", "")
    }

    given("an escaped template") {
        val t = "\$\${foo}"
        on("Template().substitute('$t')") {
            val rendered = Template().substitute(t, defaults)
            it("should return the un-escaped input '\${foo}'") {
                assertEquals("\${foo}", rendered)
            }
        }
    }

    given("an invalid template") {
        listOf(
                "\${",
                "\$}",
                "\${}",
                "\${ }",
                "\${ foo}",
                "\${foo }",
                "\${foo!}"
        ).forEach { t ->
            on("Template().substitute('$t')") {
                it("should fail") {
                    assertFailsWith(Exception::class, { Template().substitute(t, defaults) })
                }
            }
        }
    }

    given("a template without value and without default") {
        listOf(
                "This \${missing} var",
                "This \${BAR} var"
        ).forEach { t ->
            on("Template().substitute('$t')") {
                val rendered = Template().substitute(t, defaults)
                it("should render 'This  var'") {
                    assertEquals("This  var", rendered)
                }
            }
        }
    }

    given("a template with value and without default") {
        listOf(
                "This \$FOO var",
                "This \${FOO} var"
        ).forEach { t ->
            on("Template().substitute('$t')") {
                val rendered = Template().substitute(t, defaults)
                it("should render 'This first var'") {
                    assertEquals("This first var", rendered)
                }
            }
        }
    }

    given("a template without value but with default") {
        listOf(
                "ok \${missing:-def}",
                "ok \${missing-def}"
        ).forEach { t ->
            on("Template().substitute('$t')") {
                val rendered = Template().substitute(t, defaults)
                it("should render 'ok def'") {
                    assertEquals("ok def", rendered)
                }
            }
        }
    }

    given("a template with empty value but with soft default") {
        val t = "ok \${BAR:-def}"
        on("Template().substitute('$t')") {
            val rendered = Template().substitute(t, defaults)
            it("should render 'ok def'") {
                assertEquals("ok def", rendered)
            }
        }
    }

    given("a template with empty value but with hard default") {
        val t = "ok \${BAR-def}"
        on("Template().substitute('$t')") {
            val rendered = Template().substitute(t, defaults)
            it("should render 'ok def'") {
                assertEquals("ok ", rendered)
            }
        }
    }

    given("a non alphanumeric default") {
        val t = "ok \${BAR:-/non:-alphanumeric}"
        on("Template().substitute('$t')") {
            val rendered = Template().substitute(t, defaults)
            it("should render 'ok /non:-alphanumeric'") {
                assertEquals("ok /non:-alphanumeric", rendered)
            }
        }
    }
})
