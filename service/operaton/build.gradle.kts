import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.bpmnToCode)
}

dependencies {
    implementation(libs.bundles.defaultService)
    implementation(libs.bundles.database)
    implementation(libs.bundles.operaton)
    implementation(libs.bpmn.to.code.runtime)
    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.operatonProcessTest)
    testImplementation(project(":common:common-architecture-test"))
}

configurations.testRuntimeClasspath {
    exclude(group = "org.operaton.bpm.springboot", module = "operaton-bpm-spring-boot-starter-rest")
    exclude(group = "org.operaton.bpm.webapp", module = "operaton-webapp")
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-webmvc")
    exclude(group = "org.springframework.boot", module = "spring-boot-webmvc-autoconfigure")
    exclude(group = "org.springframework.boot", module = "spring-boot-jersey-autoconfigure")
    exclude(group = "org.springframework.boot", module = "spring-boot-health")
    exclude(group = "org.springframework.boot", module = "spring-boot-health-autoconfigure")
    exclude(group = "org.springframework.boot", module = "spring-boot-jackson2")
}

tasks.register<GenerateBpmnModelsTask>("generateBpmnModels") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/bpmn/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "de.emaarco.example.adapter.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.CAMUNDA_7
}

tasks.named("classes") {
    dependsOn("generateBpmnModels")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<BootJar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

java.sourceCompatibility = JavaVersion.VERSION_21
