import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

application {
    mainClass.set("id.pilah.cli.MainKt")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))

    testImplementation(libs.junit)
}
