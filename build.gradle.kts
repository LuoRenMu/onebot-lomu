plugins {
    kotlin("jvm") version "2.0.0"
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.spring") version "2.0.0"
}



group = "cn.luorenmu"
version = "1.0-SNAPSHOT"

repositories {
    maven(url = "https://maven.aliyun.com/repository/public/")
    maven(url = "https://maven.aliyun.com/repository/spring/")

    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
dependencies {
    implementation("cn.hutool:hutool-all:5.8.29")
    implementation("com.google.zxing:core:3.5.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.mikuac:shiro:2.2.9")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")


    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation(files("lib/MultifunctionalAutoHelper-Java.jar"))
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Kotlin test dependencies
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "cn.luorenmu.MainApplication"
    }
}