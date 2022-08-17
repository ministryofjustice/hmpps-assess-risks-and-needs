package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.IncorrectInputParametersException

@Component
class OffenceCodeValidator(@Qualifier("getSupportedOffenceCodes") private val offenceCodes: List<Int>) {

  fun validate(offence: CurrentOffenceDto): Boolean {
    return try {
      val homeOfficeCode = (offence.offenceCode + offence.offenceSubcode.padStart(2, '0')).toInt()
      offenceCodes.contains(homeOfficeCode)
    } catch (e: NumberFormatException) {
      throw IncorrectInputParametersException("Invalid offence code ${offence.offenceCode + offence.offenceSubcode}.")
    }
  }
}
