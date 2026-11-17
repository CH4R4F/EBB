package app.ebbplanner.lint

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards against Kotlin string templates like `"$foo.bar"` that stringify the receiver
 * instead of interpolating a property (correct form: `"${foo.bar}"`).
 */
class StringTemplateGuardTest {
    private val pattern = Regex(""""\$[a-zA-Z_][a-zA-Z0-9_]*\.""")

    @Test
    fun mainSources_avoidDollarIdentifierDotTemplates() {
        val root = File("src/main/java")
        val violations = mutableListOf<String>()
        root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    if (pattern.containsMatchIn(line)) {
                        violations += "${file.relativeTo(File("."))}:${index + 1}: $line"
                    }
                }
            }
        assertTrue(
            "Fix string templates to use \${identifier.property}:\n" + violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
