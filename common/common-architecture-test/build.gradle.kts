plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(libs.bundles.konsist)
    implementation(libs.junit.jupiter)
}

java.sourceCompatibility = JavaVersion.VERSION_21
