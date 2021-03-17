import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

plugins {
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.spring") version "1.4.31"
    kotlin("kapt") version "1.4.31"
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
    implementation("io.jsonwebtoken:jjwt:0.9.+")
    implementation("javax.xml.bind:jaxb-api")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.+")
    testImplementation("org.testcontainers:testcontainers:1.15.+")
    testImplementation("org.testcontainers:junit-jupiter:1.15.+")
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.+")


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

    implementation("org.mapstruct:mapstruct:1.4.2.Final")
    kapt("org.mapstruct:mapstruct-processor:1.4.2.Final")
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

tasks.create("coverageReport") {
    dependsOn(tasks.getByName<JacocoReport>("jacocoTestReport"))
    doLast {
        val builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        val builder = builderFactory.newDocumentBuilder()
        val xmlDocument = builder.parse("build/reports/jacoco/test/jacocoTestReport.xml")
        val xPath = XPathFactory.newInstance().newXPath()
        val expression = "/report/counter[@type='INSTRUCTION']"
        val node = xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE) as Node
        val covered = node.attributes.getNamedItem("covered").nodeValue.toDouble()
        val missed = node.attributes.getNamedItem("missed").nodeValue.toDouble()
        val total = missed + covered
        val percentage = covered / total * 100
        println(String.format("Missed %.0f branches", missed))
        println(String.format("Covered %.0f branches", covered))
        println(String.format("Total %.0f%%", percentage))
    }
}


//tasks.create<Test>("integrationTest") {
//    description = "Runs the integration tests."
//    group = "verification"
//    testClassesDirs = sourceSets["integrationTest"].output
//    classpath = sourceSets["integrationTest"].compileClasspath
//    outputs.upToDateWhen { false }
//    mustRunAfter("test")
//}https://softeq.github.io/itest-gradle-plugin/

