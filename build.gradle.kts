plugins {
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.shadow)
    alias(libs.plugins.allure)
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
    annotationProcessor(libs.micronaut.openapi)
    annotationProcessor(libs.micronaut.validation.processor)
    annotationProcessor(libs.resilience4j.micronaut)

    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.sourcegen.annotations)
    implementation(libs.logback.classic)
    implementation(libs.micronaut.validation)
    implementation(libs.swagger.annotations)
    implementation(libs.micronaut.management)
    implementation(libs.micronaut.micrometer.core)
    implementation(libs.micronaut.micrometer.registry.prometheus)
    implementation(libs.resilience4j.micronaut)

    compileOnly(libs.micronaut.http.client)
    
    testImplementation(libs.micronaut.http.client)
    testImplementation(libs.allure.junit5)

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

allure {
    version.set("2.33.0")
    adapter {
        frameworks {
            junit5 {
                enabled.set(true)
            }
        }
    }
    report {
        singleFile.set(true)
    }
}
