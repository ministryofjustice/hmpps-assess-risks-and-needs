package uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto

// Internal Service Exceptions
class EntityNotFoundException(msg: String?) : RuntimeException(msg)
class DuplicateSourceRecordFound(msg: String?, val supplementaryRiskDto: SupplementaryRiskDto? = null) : RuntimeException(msg)
