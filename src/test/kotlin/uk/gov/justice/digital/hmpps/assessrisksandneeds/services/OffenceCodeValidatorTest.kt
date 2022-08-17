package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.IncorrectInputParametersException

@ExtendWith(MockKExtension::class)
class OffenceCodeValidatorTest {

  @Test
  fun `should return true if home office code found in offence codes`() {
    // Given
    val offenceCodes = listOf(12345, 23456, 34567)
    val offenceCodeValidator = OffenceCodeValidator(offenceCodes)

    // When
    val result = offenceCodeValidator.validate(CurrentOffenceDto("234", "56"))

    // Then
    assertThat(result).isTrue
  }

  @Test
  fun `should return false if home office code not found in offence codes`() {
    // Given
    val offenceCodes = listOf(12345, 23456, 34567)
    val offenceCodeValidator = OffenceCodeValidator(offenceCodes)

    // When
    val result = offenceCodeValidator.validate(CurrentOffenceDto("456", "78"))

    // Then
    assertThat(result).isFalse
  }

  @Test
  fun `should zero pad offence sub-code if less than two digits`() {
    // Given
    val offenceCodes = listOf(12345, 23405, 34567)
    val offenceCodeValidator = OffenceCodeValidator(offenceCodes)

    // When
    val result = offenceCodeValidator.validate(CurrentOffenceDto("234", "5"))

    // Then
    assertThat(result).isTrue
  }

  @Test
  fun `should remove leading zeros from offence code`() {
    // Given
    val offenceCodes = listOf(12345, 23405, 34567)
    val offenceCodeValidator = OffenceCodeValidator(offenceCodes)

    // When
    val result = offenceCodeValidator.validate(CurrentOffenceDto("00234", "05"))

    // Then
    assertThat(result).isTrue
  }

  @Test
  fun `should throw exception when invalid offence code is provided`() {
    // Given
    val offenceCodes = listOf(12345, 23405, 34567)
    val offenceCodeValidator = OffenceCodeValidator(offenceCodes)

    // When Then
    val exception = assertThrows<IncorrectInputParametersException> {
      offenceCodeValidator.validate(CurrentOffenceDto("00234X", "05"))
    }
    assertThat(exception.message).isEqualTo("Invalid offence code 00234X05.")
  }
}
