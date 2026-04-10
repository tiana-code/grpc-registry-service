import com.google.protobuf.gradle.id

plugins {
    java
    id("io.micronaut.application") version "4.4.4"
    id("com.google.protobuf") version "0.9.4"
}

group = "com.grpcregistry"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val grpcVersion = "1.63.0"
val protobufVersion = "3.25.3"

micronaut {
    version("4.4.3")
    runtime("netty")
    processing {
        incremental(true)
        annotations("com.grpcregistry.*")
    }
}

dependencies {
    // Annotation processors — Lombok FIRST
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("io.micronaut.data:micronaut-data-processor")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")

    // Micronaut core
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("jakarta.validation:jakarta.validation-api")

    // Data + JPA
    implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")

    // gRPC
    implementation("io.micronaut.grpc:micronaut-grpc-server-runtime")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")

    // Lombok
    compileOnly("org.projectlombok:lombok")

    // Runtime
    runtimeOnly("com.h2database:h2")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")

    // Test
    testAnnotationProcessor("io.micronaut:micronaut-inject-java")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("org.assertj:assertj-core")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}

application {
    mainClass = "com.grpcregistry.GrpcRegistryApplication"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
