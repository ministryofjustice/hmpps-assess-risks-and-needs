import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.9"
  id("org.jetbrains.kotlin.kapt") version "2.4.20"
  kotlin("plugin.spring") version "2.4.20"
  kotlin("plugin.jpa") version "2.4.20"
  id("org.jetbrains.kotlinx.kover") version "0.9.9"
}

sourceSets {
  create("integrationTest") {
    compileClasspath += sourceSets["main"].output + sourceSets["test"].output
    runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
  }
}

configurations.named("integrationTestImplementation") {
  extendsFrom(configurations.testImplementation.get())
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
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.1")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.1")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
  implementation("com.zaxxer:HikariCP:7.0.2")
  implementation("com.beust:klaxon:5.6")
  implementation("org.apache.commons:commons-lang3:3.20.0")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.postgresql:postgresql:42.7.13")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.boot:spring-boot-webtestclient")
  testImplementation("com.ninja-squad:springmockk:5.0.1")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.13.0")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
}

kotlin {
  jvmToolchain(25)
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_25
  }
  withType<BootRun> {
    jvmArgs = listOf(
      "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
    )
  }
}

tasks.test {
  exclude("**/src/integrationTest/**")
}

tasks.register<Test>("integrationTest") {
  description = "Runs integration tests."
  group = "verification"

  testClassesDirs = sourceSets["integrationTest"].output.classesDirs
  classpath = sourceSets["integrationTest"].runtimeClasspath

  // Optional: Force tests to run even if outputs haven't changed
  outputs.upToDateWhen { false }

  useJUnitPlatform()
}

tasks.named("integrationTest") {
  onlyIf {
    !gradle.startParameter.taskNames.any { it.contains("koverHtmlReport") }
  }
}

tasks.check {
  dependsOn(tasks.named("integrationTest"))
}

// this is to address JLLeitschuh/ktlint-gradle#809
ktlint {
  version = "1.5.0"
}
