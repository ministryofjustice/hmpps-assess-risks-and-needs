plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.13.0"
  kotlin("plugin.spring") version "1.9.22"
  kotlin("plugin.jpa") version "1.9.22"
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
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:2.2.1")
  implementation("org.apache.commons:commons-lang3")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
  implementation("com.zaxxer:HikariCP:6.2.1")
  implementation("org.postgresql:postgresql:42.7.1")
  implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
  implementation("com.beust:klaxon:5.6")
  implementation("net.logstash.logback:logstash-logback-encoder:7.4")
  runtimeOnly("com.h2database:h2:2.2.224")
  runtimeOnly("org.flywaydb:flyway-core")
  testRuntimeOnly("com.h2database:h2:2.2.224")
  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(module = "mockito-core")
  }
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:3.0.1")
}
repositories {
  mavenCentral()
}
java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}
