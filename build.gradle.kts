plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.13"
  kotlin("plugin.spring") version "1.5.31"
  kotlin("plugin.jpa") version "1.5.31"
}

configurations {
  implementation { exclude(mapOf("module" to "tomcat-jdbc")) }
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("org.apache.commons:commons-lang3")
  implementation("org.springdoc:springdoc-openapi-ui:1.5.10")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.5.10")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.5.10")
  implementation("com.zaxxer:HikariCP:5.0.0")
  implementation("org.postgresql:postgresql")
  implementation("com.vladmihalcea:hibernate-types-52:2.12.1")
  implementation("com.beust:klaxon:5.5")
  implementation("com.microsoft.onnxruntime:onnxruntime:1.9.0")
  runtimeOnly("com.h2database:h2:1.4.200")
  runtimeOnly("org.flywaydb:flyway-core:7.15.0")
  testRuntimeOnly("com.h2database:h2:1.4.200")
  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(module = "mockito-core")
  }
  testImplementation("com.ninja-squad:springmockk:3.0.1")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
}
