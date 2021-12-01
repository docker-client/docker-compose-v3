package de.gesellix.docker.compose.interpolation

import io.kotest.core.spec.style.DescribeSpec
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TemplateTest : DescribeSpec({

  describe("Template") {
    val defaults = hashMapOf<String, String>().apply {
      put("FOO", "first")
      put("BAR", "")
    }

    context("an escaped template") {
      val t = "\$\${foo}"
      val rendered = Template().substitute(t, defaults)

      it("should return the un-escaped input '\${foo}'") {
        assertEquals("\${foo}", rendered)
      }
    }

    context("an invalid template") {
      listOf(
        "\${",
        "\$}",
        "\${}",
        "\${ }",
        "\${ foo}",
        "\${foo }",
        "\${foo!}"
      ).forEach { t ->
        it("should fail for $t") {
          assertFailsWith(Exception::class) { Template().substitute(t, defaults) }
        }
      }
    }

    context("a template without value and without default") {
      listOf(
        "This \${missing} var",
        "This \${BAR} var"
      ).forEach { t ->
        val rendered = Template().substitute(t, defaults)

        it("should render $t as 'This  var'") {
          assertEquals("This  var", rendered)
        }
      }
    }

    context("a template with value and without default") {
      listOf(
        "This \$FOO var",
        "This \${FOO} var"
      ).forEach { t ->
        val rendered = Template().substitute(t, defaults)

        it("should render $t as 'This first var'") {
          assertEquals("This first var", rendered)
        }
      }
    }

    context("a template without value but with default") {
      listOf(
        "ok \${missing:-def}",
        "ok \${missing-def}"
      ).forEach { t ->
        val rendered = Template().substitute(t, defaults)

        it("should render $t as 'ok def'") {
          assertEquals("ok def", rendered)
        }
      }
    }

    context("a template with empty value but with soft default") {
      val t = "ok \${BAR:-def}"
      val rendered = Template().substitute(t, defaults)

      it("should render $t as 'ok def'") {
        assertEquals("ok def", rendered)
      }
    }

    context("a template with empty value but with hard default") {
      val t = "ok \${BAR-def}"
      val rendered = Template().substitute(t, defaults)

      it("should render $t as 'ok def'") {
        assertEquals("ok ", rendered)
      }
    }

    context("a non alphanumeric default") {
      val t = "ok \${BAR:-/non:-alphanumeric}"
      val rendered = Template().substitute(t, defaults)

      it("should render $t as 'ok /non:-alphanumeric'") {
        assertEquals("ok /non:-alphanumeric", rendered)
      }
    }
  }
})
