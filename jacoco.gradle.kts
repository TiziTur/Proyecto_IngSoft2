// ─────────────────────────────────────────────────────────────────────────────
// jacoco.gradle.kts — Configuración de cobertura de código con JaCoCo
// Referencia IEEE 730 SQA: Sección 5.3 — Métricas de calidad
// Cobertura mínima: 70% de instrucciones (equivalente a pytest --cov en Python)
// ─────────────────────────────────────────────────────────────────────────────

val hasAndroid = plugins.hasPlugin("com.android.library") || plugins.hasPlugin("com.android.application")
val unitTestTaskName = if (hasAndroid) "testDebugUnitTest" else "test"

// Filtro compartido — excluye clases que no corresponde medir con unit tests
val sharedFileFilter = listOf(
    // Clases generadas por Android build
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    // Hilt generado
    "**/*_HiltModules*",
    "**/*Hilt_*",
    "**/*_Factory*",
    "**/*_MembersInjector*",
    // Room generado
    "**/*_Impl*",
    "**/*Dao_Impl*",
    "**/*Database_Impl*",
    // UI de Jetpack Compose — Composables no son testeables con unit tests
    "**/*Screen*",
    "**/*Activity*",
    "**/*Fragment*",
    "**/*Navigation*",
    "**/*Theme*",
    "**/*Color*",
    "**/*Typography*",
    "**/*ComposableSingletons*",
    "**/*Kt\$*",           // lambdas internas generadas por Compose
    "**/*\$\$serializer*"  // kotlinx.serialization
)

fun buildClassDirectories(project: Project, filter: List<String>): FileCollection {
    val debugTree = project.fileTree("${project.layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
        exclude(filter)
    }
    val kotlinDebugTree = project.fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(filter)
    }
    val jvmTree = project.fileTree("${project.layout.buildDirectory.get()}/classes/kotlin/main") {
        exclude(filter)
    }
    return project.files(debugTree, kotlinDebugTree, jvmTree)
}

fun buildExecutionData(project: Project): FileTree {
    return project.fileTree(project.layout.buildDirectory.get()) {
        include(
            "jacoco/testDebugUnitTest.exec",
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
            "jacoco/test.exec"
        )
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(unitTestTaskName)
    group = "Reporting"
    description = "Genera reporte HTML de cobertura de código con JaCoCo"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    classDirectories.setFrom(buildClassDirectories(project, sharedFileFilter))
    executionData.setFrom(buildExecutionData(project))
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    group = "Verification"
    description = "Verifica que la cobertura de código sea >= 70% (IEEE 730 §5.3)"

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    classDirectories.setFrom(buildClassDirectories(project, sharedFileFilter))
    executionData.setFrom(buildExecutionData(project))

    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()  // 70% mínimo
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal()  // 60% mínimo en branches
            }
        }
    }
}
