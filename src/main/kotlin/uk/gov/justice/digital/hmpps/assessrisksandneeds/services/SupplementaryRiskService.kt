package uk.gov.justice.digital.hmpps.assessrisksandneeds.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.Source
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.SupplementaryRiskDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.UserType
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.entities.SupplementaryRiskEntity
import uk.gov.justice.digital.hmpps.assessrisksandneeds.jpa.respositories.SupplementaryRiskRepository
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.exceptions.EntityNotFoundException

@Service
class SupplementaryRiskService(
  private val supplementaryRiskRepository: SupplementaryRiskRepository
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getRiskBySourceAndSourceId(source: Source, sourceId: String): SupplementaryRiskDto {
    log.info("Get supplementary risk data by source:$source and sourceId:$sourceId")
    val supplementaryRiskEntity = supplementaryRiskRepository.findBySourceAndSourceId(source.name, sourceId)
    return supplementaryRiskEntity.toSupplementaryRiskDto("for source:$source and sourceId:$sourceId")
  }


  fun SupplementaryRiskEntity?.toSupplementaryRiskDto(message: String): SupplementaryRiskDto {
    if (this == null) throw EntityNotFoundException("Error retrieving Supplementary Risk $message")
    return SupplementaryRiskDto(
      this.supplementaryRiskUuid,
      Source.fromString(this.source),
      this.sourceId,
      this.crn,
      this.createdBy,
      UserType.fromString(this.createdByUserType),
      this.createdDate,
      this.riskComments
    )
  }
}