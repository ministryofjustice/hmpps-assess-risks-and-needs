package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@AnalyzeClasses(packages = ["uk.gov.justice.digital.hmpps.assessrisksandneeds"], importOptions = [ImportOption.DoNotIncludeTests::class])
class ClockTest {
  @ArchTest
  fun testNoCallsDirectlyToNow(importedClasses: JavaClasses) {
    val rule = noClasses()
      .that().doNotHaveFullyQualifiedName(Clock::class.java.canonicalName)
      .should()
      .callMethod(LocalDateTime::class.java, "now")
      .orShould()
      .callMethod(LocalDate::class.java, "now")
      .orShould()
      .callMethod(Instant::class.java, "now")
      .because("We should only use the Clock bean to access now()")
    rule.check(importedClasses)
  }
}
