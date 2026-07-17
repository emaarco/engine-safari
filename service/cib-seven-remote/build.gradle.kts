import io.miragon.bpmn.adapter.GenerateBpmnModelsTask
import io.miragon.bpmn.domain.shared.OutputLanguage
import io.miragon.bpmn.domain.shared.ProcessEngine
import org.gradle.kotlin.dsl.withType
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.bpmnToCode)
}

dependencies {
    implementation(libs.bundles.defaultService)
    implementation(libs.cib7.external.task.client)
    implementation(libs.bpmn.to.code.runtime)
    // The external-task client (de)serializes typed variables via JAXB, which is
    // not on the classpath of a standalone worker – provide it explicitly.
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime")
    testImplementation(libs.bundles.test)
    testImplementation("de.emaarco.example:common-architecture-test")
}

tasks.register<GenerateBpmnModelsTask>("generateBpmnModels") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/bpmn/bike-order.bpmn"
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
    forkEvery = 1
}

tasks.withType<BootJar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

java.sourceCompatibility = JavaVersion.VERSION_21
