import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.0"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.spring") version "1.4.10"
    id("jacoco")
}

group = "de.oglimmer"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("javax.xml.bind:jaxb-api")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.2")
    testImplementation("org.testcontainers:testcontainers:1.15.0")
    testImplementation("org.testcontainers:junit-jupiter:1.15.0")

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.getByName<JacocoReport>("jacocoTestReport") {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
    afterEvaluate {
        classDirectories.from(files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/config/**", "**/entity/**")
            }
        }))
    }
}

//tasks.create("coverageReport") {
//    dependsOn(tasks.getByName<JacocoReport>("jacocoTestReport"))
//
//    val reportFile = project.file("build/reports/jacoco/test/jacocoTestReport.xml")
//    inputs.file(reportFile)
//
//    doLast {
//        val slurper = groovy.util.XmlSlurper()
//        slurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
//        slurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
//        val xml = slurper.parse(reportFile)
//        val counter = xml.counter.find { node ->
//            node.@ type == 'BRANCH'
//        }
//        val missed = counter.@ missed . toDouble ()
//        val covered = counter.@ covered . toDouble ()
//        val total = missed + covered
//        val percentage = covered / total * 100
//
//        println(String.format("Missed %.0f branches%n", missed))
//        println(String.format("Covered %.0f branches%n", covered))
//        println(String.format("Total %.0f%%%n", percentage))
//    }
//}


//tasks.create<Test>("integrationTest") {
//    description = "Runs the integration tests."
//    group = "verification"
//    testClassesDirs = sourceSets["integrationTest"].output
//    classpath = sourceSets["integrationTest"].compileClasspath
//    outputs.upToDateWhen { false }
//    mustRunAfter("test")
//}https://softeq.github.io/itest-gradle-plugin/

