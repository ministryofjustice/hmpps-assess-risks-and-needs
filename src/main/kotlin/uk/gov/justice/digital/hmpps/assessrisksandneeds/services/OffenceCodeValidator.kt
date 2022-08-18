package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.CurrentOffenceDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.IncorrectInputParametersException

@Component
class OffenceCodeValidator(@Qualifier("getSupportedOffenceCodes") private val offenceCodes: List<Int>) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun validate(offence: CurrentOffenceDto): Boolean {
    return try {
      val homeOfficeCode = (offence.offenceCode + offence.offenceSubcode.padStart(2, '0')).toInt()
      offenceCodes.contains(homeOfficeCode)
    } catch (e: NumberFormatException) {
      val msg = "Invalid offence code ${offence.offenceCode + offence.offenceSubcode}."
      log.error(msg, e)
      throw IncorrectInputParametersException(msg)
    }
  }
}
