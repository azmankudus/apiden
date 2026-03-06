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
    // Annotation Processors
    annotationProcessor(libs.micronaut.http.validation)
    annotationProcessor(libs.micronaut.serde.processor)
    annotationProcessor(libs.micronaut.sourcegen.generator.java)
    annotationProcessor(libs.micronaut.openapi)
    annotationProcessor(libs.micronaut.validation.processor)

    // Core
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.sourcegen.annotations)
    implementation(libs.logback.classic)
    implementation(libs.micronaut.validation)

    // OpenAPI
    implementation(libs.swagger.annotations)

    // Management & Metrics
    implementation(libs.micronaut.management)
    implementation(libs.micronaut.micrometer.core)
    implementation(libs.micronaut.micrometer.registry.prometheus)

    // Resilience
    implementation(libs.resilience4j.micronaut)

    // Client
    compileOnly(libs.micronaut.http.client)
    
    // Testing
    testImplementation(libs.micronaut.http.client)
    testImplementation(libs.archunit.junit5)
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
    aot {
        // Optimize for native image
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
    }
}
