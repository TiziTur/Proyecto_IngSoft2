import io.gitlab.arturbosch.detekt.Detekt
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false

    // Activar el plugin de Detekt en la raíz
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

// Le decimos a Gradle que aplique Detekt y ktlint a absolutamente TODOS los módulos del proyecto
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        // Esto incluye las reglas de ktlint (formato) dentro de Detekt
        add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
    }

    detekt {
        toolVersion = "1.23.8"
        // Usa el archivo de configuración global que encontraste en la carpeta config/
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            // Guarda un reporte HTML individual dentro de la carpeta build de cada módulo
            html.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
        }
    }

    pluginManager.withPlugin("com.android.library") {
        apply(plugin = "jacoco")
        apply(from = "$rootDir/jacoco.gradle.kts")
    }

}

val detektSummaryReport = layout.buildDirectory.file("reports/detekt/detekt-summary.md")
val detektSummaryHtmlReport = layout.buildDirectory.file("reports/detekt/detekt-summary.html")

tasks.register("detektSummaryReport") {
    group = "verification"
    description = "Genera un unico reporte Detekt para todos los modulos"
    dependsOn(subprojects.map { "${it.path}:detekt" })
    outputs.file(detektSummaryReport)

    doLast {
        val outputFile = detektSummaryReport.get().asFile
        outputFile.parentFile.mkdirs()

        val content = buildString {
            appendLine("# Detekt Summary Report")
            appendLine()

            subprojects.forEach { project ->
                val reportFile = project.layout.buildDirectory
                    .file("reports/detekt/detekt.md")
                    .get()
                    .asFile

                appendLine("## ${project.path}")
                appendLine()

                if (reportFile.exists()) {
                    appendLine(reportFile.readText())
                } else {
                    appendLine("_No se genero reporte para este modulo._")
                }

                appendLine()
            }
        }

        outputFile.writeText(content)
        println("Reporte consolidado: ${outputFile.absolutePath}")
    }
}

tasks.register("detektSummaryHtmlReport") {
    group = "verification"
    description = "Genera un reporte HTML unico de Detekt para todos los modulos"
    dependsOn("detektSummaryReport")
    outputs.file(detektSummaryHtmlReport)

    doLast {
        val markdownFile = detektSummaryReport.get().asFile
        val htmlFile = detektSummaryHtmlReport.get().asFile
        htmlFile.parentFile.mkdirs()

        val escapedMarkdown = markdownFile.readText()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        val html = """
            <!doctype html>
            <html lang="es">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Detekt Summary Report</title>
              <style>
                :root {
                  color-scheme: light;
                }
                body {
                  margin: 0;
                  font-family: "JetBrains Mono", "Fira Code", "SFMono-Regular", Menlo, Consolas, monospace;
                  background: #f5f7fa;
                  color: #1f2937;
                }
                .container {
                  max-width: 1100px;
                  margin: 24px auto;
                  padding: 0 16px;
                }
                .card {
                  background: #ffffff;
                  border: 1px solid #e5e7eb;
                  border-radius: 12px;
                  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
                  overflow: hidden;
                }
                .header {
                  padding: 14px 18px;
                  background: linear-gradient(90deg, #1b5e20, #2e7d32);
                  color: #ffffff;
                  font-size: 14px;
                  font-weight: 600;
                }
                pre {
                  margin: 0;
                  padding: 16px 18px;
                  overflow-x: auto;
                  white-space: pre-wrap;
                  word-break: break-word;
                  line-height: 1.45;
                  font-size: 13px;
                }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="card">
                  <div class="header">Detekt Summary Report</div>
                  <pre>$escapedMarkdown</pre>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        htmlFile.writeText(html)
        println("Reporte HTML consolidado: ${htmlFile.absolutePath}")
    }
}

val jacocoSummaryReport = layout.buildDirectory.file("reports/jacoco/jacoco-summary.md")
val jacocoSummaryHtmlReport = layout.buildDirectory.file("reports/jacoco/jacoco-summary.html")
val testSummaryReport = layout.buildDirectory.file("reports/tests/test-summary.md")
val testSummaryHtmlReport = layout.buildDirectory.file("reports/tests/test-summary.html")

tasks.register("jacocoSummaryReport") {
    group = "verification"
    description = "Genera un reporte consolidado de cobertura JaCoCo"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("jacocoTestReport") })
    outputs.file(jacocoSummaryReport)

    doLast {
        val outputFile = jacocoSummaryReport.get().asFile
        outputFile.parentFile.mkdirs()

        val xmlFactory = DocumentBuilderFactory.newInstance()
        xmlFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        xmlFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        xmlFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        xmlFactory.isExpandEntityReferences = false
        val xmlBuilder = xmlFactory.newDocumentBuilder()
        var totalMissedInstructions = 0
        var totalCoveredInstructions = 0
        var totalMissedBranches = 0
        var totalCoveredBranches = 0

        val moduleLines = buildList {
            subprojects.forEach { project ->
                val reportXml = project.layout.buildDirectory
                    .file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
                    .get()
                    .asFile

                if (!reportXml.exists()) {
                    add("- ${project.path}: INSTRUCTION=0.00% | BRANCH=0.00%")
                    return@forEach
                }

                val doc = xmlBuilder.parse(reportXml)
                val counters = doc.getElementsByTagName("counter")
                var instructionMissed = 0
                var instructionCovered = 0
                var branchMissed = 0
                var branchCovered = 0

                for (i in 0 until counters.length) {
                    val node = counters.item(i)
                    val type = node.attributes.getNamedItem("type")?.nodeValue ?: continue
                    val missed = node.attributes.getNamedItem("missed")?.nodeValue?.toIntOrNull() ?: 0
                    val covered = node.attributes.getNamedItem("covered")?.nodeValue?.toIntOrNull() ?: 0
                    if (type == "INSTRUCTION") {
                        instructionMissed = missed
                        instructionCovered = covered
                    }
                    if (type == "BRANCH") {
                        branchMissed = missed
                        branchCovered = covered
                    }
                }

                totalMissedInstructions += instructionMissed
                totalCoveredInstructions += instructionCovered
                totalMissedBranches += branchMissed
                totalCoveredBranches += branchCovered

                val instructionRatio = if (instructionCovered + instructionMissed > 0) {
                    instructionCovered.toDouble() / (instructionCovered + instructionMissed)
                } else 0.0
                val branchRatio = if (branchCovered + branchMissed > 0) {
                    branchCovered.toDouble() / (branchCovered + branchMissed)
                } else 0.0

                add(
                    "- ${project.path}: INSTRUCTION=${"%.2f".format(instructionRatio * 100)}% " +
                        "| BRANCH=${"%.2f".format(branchRatio * 100)}%"
                )
            }
        }

        val totalInstructionRatio = if (totalCoveredInstructions + totalMissedInstructions > 0) {
            totalCoveredInstructions.toDouble() / (totalCoveredInstructions + totalMissedInstructions)
        } else 0.0
        val totalBranchRatio = if (totalCoveredBranches + totalMissedBranches > 0) {
            totalCoveredBranches.toDouble() / (totalCoveredBranches + totalMissedBranches)
        } else 0.0

        val content = buildString {
            appendLine("# JaCoCo Summary Report")
            appendLine()
            appendLine("## Global")
            appendLine("- INSTRUCTION=${"%.2f".format(totalInstructionRatio * 100)}%")
            appendLine("- BRANCH=${"%.2f".format(totalBranchRatio * 100)}%")
            appendLine()
            appendLine("## By Module")
            if (moduleLines.isEmpty()) {
                appendLine("- No se encontraron reportes JaCoCo por modulo.")
            } else {
                moduleLines.forEach { appendLine(it) }
            }
        }

        outputFile.writeText(content)
        println("Reporte JaCoCo consolidado: ${outputFile.absolutePath}")
    }
}

tasks.register("jacocoSummaryHtmlReport") {
    group = "verification"
    description = "Genera un reporte HTML unico de JaCoCo"
    dependsOn("jacocoSummaryReport")
    outputs.file(jacocoSummaryHtmlReport)

    doLast {
        val markdownFile = jacocoSummaryReport.get().asFile
        val htmlFile = jacocoSummaryHtmlReport.get().asFile
        htmlFile.parentFile.mkdirs()

        val escaped = markdownFile.readText()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        val html = """
            <!doctype html>
            <html lang="es">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>JaCoCo Summary Report</title>
              <style>
                body { margin: 0; font-family: "JetBrains Mono", monospace; background: #f5f7fa; color: #1f2937; }
                .container { max-width: 960px; margin: 24px auto; padding: 0 16px; }
                .card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden; }
                .header { padding: 14px 18px; background: linear-gradient(90deg, #0d47a1, #1976d2); color: #fff; font-weight: 600; }
                pre { margin: 0; padding: 16px 18px; white-space: pre-wrap; line-height: 1.45; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="card">
                  <div class="header">JaCoCo Summary Report</div>
                  <pre>$escaped</pre>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        htmlFile.writeText(html)
        println("Reporte JaCoCo HTML consolidado: ${htmlFile.absolutePath}")
    }
}

tasks.register("testSummaryReport") {
    group = "verification"
    description = "Genera un resumen consolidado de tests por modulo"
    dependsOn(
        subprojects.mapNotNull { project ->
            if (project.plugins.hasPlugin("com.android.library")) {
                project.tasks.findByName("testDebugUnitTest")
            } else {
                null
            }
        }
    )
    outputs.file(testSummaryReport)

    doLast {
        val outputFile = testSummaryReport.get().asFile
        outputFile.parentFile.mkdirs()

        val xmlFactory = DocumentBuilderFactory.newInstance()
        xmlFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        xmlFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        xmlFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        xmlFactory.isExpandEntityReferences = false
        val xmlBuilder = xmlFactory.newDocumentBuilder()

        var totalTests = 0
        var totalFailures = 0
        var totalSkipped = 0

        val moduleLines = buildList {
            val failedTestsByModule = linkedMapOf<String, MutableList<String>>()

            subprojects.forEach { project ->
                val testsDir = project.layout.buildDirectory
                    .dir("test-results/testDebugUnitTest")
                    .get()
                    .asFile

                val xmlFiles = testsDir.listFiles { _, name -> name.endsWith(".xml") }?.toList().orEmpty()

                if (xmlFiles.isEmpty()) {
                    add("- ${project.path}: tests=0 | passed=0 | failed=0 | skipped=0")
                    return@forEach
                }

                var tests = 0
                var failures = 0
                var skipped = 0
                val failedTests = mutableListOf<String>()

                xmlFiles.forEach { xml ->
                    val doc = xmlBuilder.parse(xml)
                    val suite = doc.documentElement
                    tests += suite.getAttribute("tests").toIntOrNull() ?: 0
                    failures += (suite.getAttribute("failures").toIntOrNull() ?: 0) +
                        (suite.getAttribute("errors").toIntOrNull() ?: 0)
                    skipped += suite.getAttribute("skipped").toIntOrNull() ?: 0

                    val testCases = doc.getElementsByTagName("testcase")
                    for (i in 0 until testCases.length) {
                        val testCase = testCases.item(i)
                        val children = testCase.childNodes
                        var hasFailure = false
                        for (j in 0 until children.length) {
                            val child = children.item(j)
                            if (child.nodeName == "failure" || child.nodeName == "error") {
                                hasFailure = true
                                break
                            }
                        }

                        if (hasFailure) {
                            val className = testCase.attributes.getNamedItem("classname")?.nodeValue ?: "UnknownClass"
                            val testName = testCase.attributes.getNamedItem("name")?.nodeValue ?: "unknownTest"
                            failedTests.add("$className#$testName")
                        }
                    }
                }

                val passed = tests - failures - skipped
                totalTests += tests
                totalFailures += failures
                totalSkipped += skipped

                if (failedTests.isNotEmpty()) {
                    failedTestsByModule[project.path] = failedTests
                }

                add("- ${project.path}: tests=$tests | passed=$passed | failed=$failures | skipped=$skipped")
            }

            this@register.extensions.extraProperties["failedTestsByModule"] = failedTestsByModule
        }

        val totalPassed = totalTests - totalFailures - totalSkipped

        val content = buildString {
            appendLine("# Test Summary Report")
            appendLine()
            appendLine("## Global")
            appendLine("- tests=$totalTests")
            appendLine("- passed=$totalPassed")
            appendLine("- failed=$totalFailures")
            appendLine("- skipped=$totalSkipped")
            appendLine()
            appendLine("## By Module")
            moduleLines.forEach { appendLine(it) }

            val failedTestsByModule =
                this@register.extensions.extraProperties["failedTestsByModule"] as Map<*, *>
            appendLine()
            appendLine("## Failed Tests")
            if (failedTestsByModule.isEmpty()) {
                appendLine("- None")
            } else {
                failedTestsByModule.forEach { (module, testsList) ->
                    appendLine("- $module")
                    (testsList as List<*>).forEach { test ->
                        appendLine("  - $test")
                    }
                }
            }
        }

        outputFile.writeText(content)
        println("Reporte tests consolidado: ${outputFile.absolutePath}")
    }
}

tasks.register("testSummaryHtmlReport") {
    group = "verification"
    description = "Genera un reporte HTML unico de tests"
    dependsOn("testSummaryReport")
    outputs.file(testSummaryHtmlReport)

    doLast {
        val markdownFile = testSummaryReport.get().asFile
        val htmlFile = testSummaryHtmlReport.get().asFile
        htmlFile.parentFile.mkdirs()

        val escaped = markdownFile.readText()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        val html = """
            <!doctype html>
            <html lang="es">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Test Summary Report</title>
              <style>
                body { margin: 0; font-family: "JetBrains Mono", monospace; background: #f5f7fa; color: #1f2937; }
                .container { max-width: 960px; margin: 24px auto; padding: 0 16px; }
                .card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden; }
                .header { padding: 14px 18px; background: linear-gradient(90deg, #6d4c41, #8d6e63); color: #fff; font-weight: 600; }
                pre { margin: 0; padding: 16px 18px; white-space: pre-wrap; line-height: 1.45; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="card">
                  <div class="header">Test Summary Report</div>
                  <pre>$escaped</pre>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        htmlFile.writeText(html)
        println("Reporte tests HTML consolidado: ${htmlFile.absolutePath}")
    }
}
