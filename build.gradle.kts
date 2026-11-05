import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.10"
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
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.2")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.1")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
  implementation("com.zaxxer:HikariCP:7.1.0")
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
  testImplementation("com.tngtech.archunit:archunit-junit5:1.5.0")
  testImplementation("au.com.dius.pact.provider:junit5spring:4.7.5")
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
  exclude("**/integration/pact/**")
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

tasks.register<Test>("pactTest") {
  description = "Run and publish Pact provider tests"
  group = "verification"

  val testSourceSet = sourceSets["test"]

  testClassesDirs = testSourceSet.output.classesDirs
  classpath = testSourceSet.runtimeClasspath

  filter {
      includeTestsMatching("*PactTest")
  }

  systemProperty("pactbroker.url", System.getenv("PACT_BROKER_URL"))
  systemProperty("pactbroker.auth.username", System.getenv("PACT_BROKER_USERNAME") ?: "")
  systemProperty("pactbroker.auth.password", System.getenv("PACT_BROKER_PASSWORD") ?: "")
//  val consumerBranch = System.getenv("PACT_CONSUMER_BRANCH") ?: ""
//  val requestedConsumerName = System.getenv("PACT_CONSUMER_NAME") ?: ""
//  val selectors = when {
//    consumerBranch.isNotEmpty() && requestedConsumerName.isEmpty() ->
//      throw GradleException("PACT_CONSUMER_NAME must be set when PACT_CONSUMER_BRANCH is provided so only the matching Pact consumer verification runs.")
//    consumerBranch.isNotEmpty() && requestedConsumerName.isNotEmpty() ->
//      """[{"consumer":"$requestedConsumerName","branch":"$consumerBranch"}]"""
//    requestedConsumerName.isNotEmpty() ->
//      """[{"consumer":"$requestedConsumerName","mainBranch":true},{"consumer":"$requestedConsumerName","deployed":true}]"""
//    else ->
//      """[{"deployed":true}]"""
//  }
//  systemProperty("pactbroker.consumerversionselectors.rawjson", selectors)

  systemProperty("pactbroker.providerBranch", System.getenv("GITHUB_BRANCH") ?: "local")
  systemProperty("pact.verifier.publishResults", System.getenv("PACT_PUBLISH_RESULTS") ?: "false")
  systemProperty("pact.provider.version", System.getenv("PACT_PROVIDER_APP_VERSION") ?: "local")
  systemProperty("pact.provider.branch", System.getenv("GITHUB_BRANCH") ?: "local")
}

tasks.named("pactTest") {
  onlyIf {
    !gradle.startParameter.taskNames.any { it.contains("koverHtmlReport") }
  }
}

// this is to address JLLeitschuh/ktlint-gradle#809
ktlint {
  version = "1.5.0"
}
