plugins {
    kotlin("jvm") version "2.0.0"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.spring") version "2.0.0"
}


repositories {
    maven(url = "https://maven.aliyun.com/repository/public/")
    maven(url = "https://maven.aliyun.com/repository/spring/")
    maven(url = "https://jitpack.io")
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
    enabled = false
}



dependencies {
    implementation("org.freemarker:freemarker:2.3.34")
    implementation("com.microsoft.playwright:playwright:1.42.0")
    // 为petpet提供的包支持
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("com.jhlabs:filters:2.0.235-1")
    implementation("com.madgag:animated-gif-lib:1.4")
    implementation("com.pngencoder:pngencoder:0.15.0")


    implementation("io.ktor:ktor-client-core:3.2.3")
    implementation("io.ktor:ktor-client-cio:3.2.3")

    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.58")


    // petpet
    implementation(files("lib/petpet-core-1.0.0-beta2.jar"))
    // 日志
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")


    // kotlin官方库 反射
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // kotlin官方库 扩展函数协程
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // spring
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-aop")
    // onebot协议库
    implementation("com.mikuac:shiro:2.4.8")

    // 转拼音、简繁转换
    implementation("com.github.promeg:tinypinyin:2.0.3")

    // kotlin 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    // spirng 测试
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "cn.luorenmu.MainApplication"
    }
}


