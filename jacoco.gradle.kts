// ─────────────────────────────────────────────────────────────────────────────
// jacoco.gradle.kts — Configuración de cobertura de código con JaCoCo
// Referencia IEEE 730 SQA: Sección 5.3 — Métricas de calidad
// Cobertura mínima: 70% de instrucciones (equivalente a pytest --cov en Python)
// ─────────────────────────────────────────────────────────────────────────────

val hasAndroid = plugins.hasPlugin("com.android.library") || plugins.hasPlugin("com.android.application")
val unitTestTaskName = if (hasAndroid) "testDebugUnitTest" else "test"

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(unitTestTaskName)
    group = "Reporting"
    description = "Genera reporte HTML de cobertura de código con JaCoCo"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        // Excluir clases generadas
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        // Excluir Hilt generado
        "**/*_HiltModules*",
        "**/*Hilt_*",
        "**/*_Factory*",
        "**/*_MembersInjector*",
        // Excluir Room generado
        "**/*_Impl*",
        "**/*Dao_Impl*",
        "**/*Database_Impl*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
    }
    val kotlinDebugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val jvmTree = fileTree("${layout.buildDirectory.get()}/classes/kotlin/main") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    classDirectories.setFrom(files(debugTree, kotlinDebugTree, jvmTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include(
            "jacoco/testDebugUnitTest.exec",
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
            "jacoco/test.exec"
        )
    })
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    group = "Verification"
    description = "Verifica que la cobertura de código sea >= 70% (IEEE 730 §5.3)"

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
