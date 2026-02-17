import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
}

dependencies {
    implementation(libs.bundles.test)
    implementation(libs.bundles.konsist)
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

java.sourceCompatibility = JavaVersion.VERSION_21