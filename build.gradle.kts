import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "7.0.0"
  kotlin("plugin.spring") version "2.1.10"
  kotlin("plugin.jpa") version "2.1.10"
  id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

configurations {
  implementation { exclude(mapOf("module" to "tomcat-jdbc")) }
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

dependencies {

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.2.2")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
  implementation("com.zaxxer:HikariCP:6.2.1")
  implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
  implementation("com.beust:klaxon:5.6")
  implementation("org.apache.commons:commons-lang3:3.17.0")
  implementation("org.postgresql:postgresql:42.7.5")
  implementation("org.flywaydb:flyway-core")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  implementation("net.logstash.logback:logstash-logback-encoder:8.0")

  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(module = "mockito-core")
  }
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
  testImplementation("org.wiremock:wiremock-standalone:3.11.0")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_21
  }
  withType<BootRun> {
    jvmArgs = listOf(
      "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
    )
  }
}

// this is to address JLLeitschuh/ktlint-gradle#809
ktlint {
  version = "1.5.0"
}
