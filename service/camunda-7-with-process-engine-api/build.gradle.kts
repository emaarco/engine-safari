import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
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
    implementation(platform(libs.process.engine.adapter.c7.bom))
    implementation(libs.bundles.defaultService)
    implementation(libs.bundles.database)
    implementation(libs.camunda7.webapp)
    implementation(libs.bundles.processEngineApi)
    testImplementation(libs.bundles.test)
    testImplementation(project(":common:common-architecture-test"))
}

tasks.register<GenerateBpmnModelsTask>("generateBpmnModels") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/bpmn/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "de.emaarco.example.adapter.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.CAMUNDA_7
    useVersioning = false
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
