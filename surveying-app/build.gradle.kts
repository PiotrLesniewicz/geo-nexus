plugins {
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"
}

val mapstructVersion: String by project
val lombokBindingVersion: String by project

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    // 1. Internal Modules (implementation)
    implementation(project(":surveying-math"))

    // 2. External Libraries (implementation)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")

    // 3. Compile Only (Lombok)
    compileOnly("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")

    // 4. Runtime Only
    runtimeOnly("org.postgresql:postgresql")

    // 5. Annotation Processors (Grouped together)
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokBindingVersion")
    testAnnotationProcessor("org.projectlombok:lombok")

    // 6. Development Only
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // 7. Testing (testImplementation)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "-Amapstruct.defaultComponentModel=spring",
        "-Amapstruct.unmappedTargetPolicy=IGNORE",
        "-Amapstruct.defaultInjectionStrategy=constructor"
    ))
}

tasks.withType<Test>().configureEach {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}