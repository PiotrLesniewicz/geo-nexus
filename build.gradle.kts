plugins {
    java
    jacoco
}

allprojects {
    group = "com.geo.survey"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")

    val jacocoVersion: String by project
    val junitVersion: String by project
    val assertJVersion: String by project

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        testImplementation("org.assertj:assertj-core:$assertJVersion")
        testImplementation(platform("org.junit:junit-bom:$junitVersion"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    configure<JacocoPluginExtension> {
        toolVersion = jacocoVersion
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        extensions.configure<JacocoTaskExtension> {
            includes = listOf("com.geo.survey.*")
            excludes = listOf(
                "**/*Test*",
                "net.bytebuddy.*",
                "com.esotericsoftware.*"
            )
        }
        finalizedBy(tasks.jacocoTestReport)
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = false
        }
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
        }
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "net/bytebuddy/**",
                        "com/esotericsoftware/**",
                        "**/*Test.class",
                        "**/*Test$*.class",
                        "**/generated/**",
                        "**/*Application.class",
                        "**/config/**",
                        "**/dto/**",
                        "**/entity/**"
                    )
                }
            })
        )
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}