plugins {
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.shadow)
}

version = "0.1"
group = "com.example"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(libs.micronaut.http.validation)
    annotationProcessor(libs.micronaut.serde.processor)
    annotationProcessor(libs.micronaut.sourcegen.generator.java)

    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.sourcegen.annotations)
    implementation(libs.logback.classic)
    
    compileOnly(libs.micronaut.http.client)
    
    testImplementation(libs.micronaut.http.client)
    
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass = "com.example.apiden.Application"
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.example.apiden.*")
    }
}
