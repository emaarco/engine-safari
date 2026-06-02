import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframeworkDepr)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.bpmnToCode)
}

dependencies {
    implementation(platform(libs.process.engine.adapter.c7.bom))
    implementation(libs.bundles.defaultServiceDepr)
    implementation(libs.bundles.databaseDepr)
    implementation(libs.camunda7.webapp)
    implementation(libs.bundles.processEngineApi)
    implementation(libs.bpmn.to.code.runtime)
    testImplementation(libs.bundles.testDepr)
    testImplementation(libs.bundles.camunda7ProcessTest)
    testImplementation(libs.bundles.camunda7JGiven)
    testImplementation(project(":common:common-architecture-test"))
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
    // Spring-Boot basic test and JGiven test each set up their own Camunda engine
    // and pollute the static ProcessEngines registry. Fork a new JVM per test class.
    forkEvery = 1
}

tasks.withType<BootJar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

java.sourceCompatibility = JavaVersion.VERSION_21
