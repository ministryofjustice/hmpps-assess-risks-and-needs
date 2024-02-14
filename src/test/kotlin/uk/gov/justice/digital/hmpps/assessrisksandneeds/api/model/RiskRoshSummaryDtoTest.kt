package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class RiskRoshSummaryDtoTest {
  @ParameterizedTest
  @MethodSource("riskLevels")
  fun `highest level correct identified`(summaryDto: RiskRoshSummaryDto, highestLevel: RiskLevel?) {
    assertThat(summaryDto.overallRiskLevel, equalTo(highestLevel))
  }

  companion object {
    @JvmStatic
    fun riskLevels() = listOf(
      Arguments.of(RiskRoshSummaryDto(), null),
      Arguments.of(
        RiskRoshSummaryDto(
          riskInCommunity = mapOf(
            RiskLevel.LOW to listOf("one", "two"),
            RiskLevel.MEDIUM to listOf("three"),
            RiskLevel.VERY_HIGH to listOf("four"),
            RiskLevel.HIGH to listOf("five"),
          ),
          riskInCustody = mapOf(
            RiskLevel.LOW to listOf("six", "seven"),
            RiskLevel.MEDIUM to listOf("eight"),
          ),
        ),
        RiskLevel.VERY_HIGH,
      ),
      Arguments.of(
        RiskRoshSummaryDto(
          riskInCommunity = mapOf(
            RiskLevel.LOW to listOf("one", "two"),
            RiskLevel.MEDIUM to listOf("three"),
          ),
          riskInCustody = mapOf(
            RiskLevel.HIGH to listOf("four"),
            RiskLevel.MEDIUM to listOf("five"),
            RiskLevel.LOW to listOf("six", "seven"),
          ),
        ),
        RiskLevel.HIGH,
      ),
    )
  }
}
