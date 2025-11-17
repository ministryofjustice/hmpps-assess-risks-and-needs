package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.controllers.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.IdentifierType

@Component
class IdentifierTypeConverter : Converter<String, IdentifierType> {

  override fun convert(source: String): IdentifierType = IdentifierType.entries.firstOrNull {
    it.name.equals(source, ignoreCase = true)
  } ?: throw IllegalArgumentException("Invalid Identifier Type: $source")
}
