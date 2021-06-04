package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SupplementaryRiskDtoTest {

  @Test
  fun `test Source from known String source returns Source`() {
    val source = Source.fromString("INTERVENTION_REFERRAL")

    assertThat(source).isEqualTo(Source.INTERVENTION_REFERRAL)
  }

  @Test
  fun `test Source from unknown String source throws Exception`() {

    val exception = assertThrows<IllegalArgumentException> {
      Source.fromString("UNKNOWN")
    }
    assertEquals(
      "Unknown Source UNKNOWN",
      exception.message
    )
  }

  @Test
  fun `test User Type from known String user type returns Source`() {
    val userType = UserType.fromString("INTERVENTIONS_PROVIDER")

    assertThat(userType).isEqualTo(UserType.INTERVENTIONS_PROVIDER)
  }

  @Test
  fun `test User Type from from unknown String user type throws Exception`() {

    val exception = assertThrows<IllegalArgumentException> {
      UserType.fromString("BLABLA")
    }
    assertEquals(
      "Unknown User Type BLABLA",
      exception.message
    )
  }
}
